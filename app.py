import os
import google.generativeai as genai
from flask import Flask, request, jsonify
from flask_cors import CORS
from dotenv import load_dotenv
import sys

# --- Firebase Admin Imports ---
import firebase_admin
from firebase_admin import credentials, messaging

# --- PostgreSQL Import ---
import psycopg2
from psycopg2 import extras  # For dictionary cursor

# Load environment variables from .env file
load_dotenv()

app = Flask(__name__)
CORS(app)  # Allow cross-origin requests

# --- CRITICAL SECURITY FIX ---
# Load ALL secrets from environment variables
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
if not GEMINI_API_KEY:
    raise ValueError("GEMINI_API_KEY environment variable not set.")

# --- PostgreSQL Connection Details ---
DB_NAME = os.getenv("DB_NAME")
DB_USER = os.getenv("DB_USER")
DB_PASS = os.getenv("DB_PASS")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = '5432'

if not all([DB_NAME, DB_USER, DB_PASS, DB_HOST]):
    print("Database environment variables are not fully set.", file=sys.stderr)
# -----------------------------------

genai.configure(api_key=GEMINI_API_KEY)

# This is the "persona" for your chatbot
SYSTEM_INSTRUCTION = (
    "You are 'Nyay Mitra' (Friend of Justice), a helpful and empathetic AI assistant. "
    "Your purpose is to help users in India understand complex legal information. "
    "You can simplify legal documents, answer general questions about legal rights, "
    "and help basic complaints or replies. "
    "When a user provides a large block of text, assume it is a legal document "
    "and your task is to simplify it and explain it in plain language. "
    "Otherwise, answer their questions clearly and simply." +
    "Do not entertain any irrelevant questions."
)

model = genai.GenerativeModel(
    model_name="gemini-2.5-flash", # Updated to a more current model
    system_instruction=SYSTEM_INSTRUCTION
)
# ----------------------------------

# --- Firebase Admin SDK Initialization ---
try:
    # CRITICAL CHANGE: Read the content from a Secret Environment Variable
    FIREBASE_CREDENTIALS_JSON = os.getenv("FIREBASE_CREDENTIALS_JSON")

    if not FIREBASE_CREDENTIALS_JSON:
        print("Firebase credentials not found in environment variable.", file=sys.stderr)
    else:
        # Use credentials.Certificate with the JSON content
        import json
        cred = credentials.Certificate(json.loads(FIREBASE_CREDENTIALS_JSON))
        firebase_admin.initialize_app(cred)
        print("Firebase Admin SDK initialized.")
except Exception as e:
    print(f"Error initializing Firebase Admin SDK: {e}", file=sys.stderr)
# ---------------------------------------

def get_db_connection():
    """Establishes a new connection to the PostgreSQL database."""
    try:
        conn = psycopg2.connect(
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASS,
            host=DB_HOST,
            port=DB_PORT,
            sslmode='require'  # Render databases require SSL
        )
        return conn
    except Exception as e:
        print(f"Error connecting to database: {e}", file=sys.stderr)
        return None
# -----------------------------------

@app.route("/")
def index():
    return "Nyay Mitra Backend is running."

@app.route("/api/chat", methods=["POST"])
def chat():
    try:
        data = request.json
        if not data or "prompt" not in data:
            return jsonify({"error": "No prompt provided"}), 400

        user_prompt = data["prompt"]

        # --- Call the Gemini API ---
        chat_session = model.start_chat(history=[])
        response = chat_session.send_message(user_prompt)
        # ---------------------------

        return jsonify({"reply": response.text})

    except Exception as e:
        print(f"Error in /api/chat: {e}", file=sys.stderr)
        return jsonify({"error": str(e)}), 500

# --- FCM Helper Function ---
def send_fcm_message(token, title, body):
    """Sends a single FCM message to a specific device token."""
    try:
        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body,
            ),
            token=token,
        )
        response = messaging.send(message)
        print("Successfully sent message:", response)
        return True, response
    except Exception as e:
        print(f"Error sending FCM message: {e}", file=sys.stderr)
        return False, str(e)
# --------------------------------

# --- Send Notification Endpoint (Existing) ---
@app.route("/api/send-notification", methods=["POST"])
def send_notification():
    try:
        data = request.json
        if not data:
            return jsonify({"error": "No JSON data provided"}), 400

        user_email = data.get("email")
        message_body = data.get("body")

        if not user_email or not message_body:
            return jsonify({"error": "Missing 'email' or 'body'"}), 400

        conn = get_db_connection()
        if not conn:
            return jsonify({"error": "Failed to connect to the database"}), 500

        token = None
        try:
            with conn.cursor() as cursor:
                # This query relies on the 'users' table you created
                cursor.execute(
                    "SELECT fcm_token FROM users WHERE email = %s", 
                    (user_email,)
                )
                result = cursor.fetchone()
                
                if result and result[0]:
                    token = result[0]
                
        except Exception as e:
            print(f"Database query error: {e}", file=sys.stderr)
            return jsonify({"error": f"Database query error: {e}"}), 500
        finally:
            conn.close() # Always close the connection

        if not token:
            return jsonify({"error": "User not found or no FCM token on file"}), 404

        success, fcm_response = send_fcm_message(
            token=token,
            title="New Message from Nyay Mitra",
            body=message_body
        )

        if success:
            return jsonify({"success": True, "message": "Notification sent.", "fcm_response": fcm_response})
        else:
            return jsonify({"error": "Failed to send notification", "fcm_error": fcm_response}), 500

    except Exception as e:
        print(f"Error in /api/send-notification: {e}", file=sys.stderr)
        return jsonify({"error": str(e)}), 500
# -------------------------------------


# ===================================================================
# --- NEW: User Login / Registration Endpoint ---
# ===================================================================
@app.route("/api/user/login-or-register", methods=["POST"])
def login_or_register_user():
    try:
        data = request.json
        if not data:
            return jsonify({"error": "No JSON data provided"}), 400

        email = data.get("email")
        fcm_token = data.get("fcm_token")
        
        # Email and FCM token are always required
        if not email or not fcm_token:
            return jsonify({"error": "Missing 'email' or 'fcm_token'"}), 400

        conn = get_db_connection()
        if not conn:
            return jsonify({"error": "Failed to connect to the database"}), 500

        try:
            # Use a dictionary cursor to get results by column name
            with conn.cursor(cursor_factory=extras.DictCursor) as cursor:
                
                # 1. Check if user exists
                cursor.execute("SELECT * FROM users WHERE email = %s", (email,))
                user = cursor.fetchone()

                if user:
                    # --- EXISTING USER ---
                    # Update their FCM token in case it changed
                    cursor.execute(
                        "UPDATE users SET fcm_token = %s WHERE email = %s",
                        (fcm_token, email)
                    )
                    conn.commit()
                    
                    user_role = user["role"]
                    return jsonify({
                        "status": "success",
                        "isNewUser": False,
                        "role": user_role,
                        "message": f"Welcome back! You are registered as a {user_role}."
                    }), 200

                else:
                    # --- NEW USER ---
                    role = data.get("role") # Role is required for new users
                    if not role:
                        return jsonify({"error": "Missing 'role' for new user"}), 400

                    # Insert new user record
                    cursor.execute(
                        "INSERT INTO users (email, role, fcm_token) VALUES (%s, %s, %s)",
                        (email, role, fcm_token)
                    )
                    conn.commit()
                    
                    return jsonify({
                        "status": "success",
                        "isNewUser": True,
                        "role": role,
                        "message": f"Welcome! You are registered as a {role}."
                    }), 201 # 201 Created

        except (Exception, psycopg2.Error) as e:
            conn.rollback() # Roll back any failed database changes
            print(f"Database error in /login-or-register: {e}", file=sys.stderr)
            return jsonify({"error": f"Database error: {e}"}), 500
        finally:
            conn.close()

    except Exception as e:
        print(f"Error in /api/user/login-or-register: {e}", file=sys.stderr)
        return jsonify({"error": str(e)}), 500
# -------------------------------------------------------------------


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.getenv("PORT", 5000)), debug=True)