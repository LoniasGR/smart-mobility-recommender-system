import requests
import json

from constants import USER_API_URL, JSON_HEADER

# Predefined List of Users
USERS = [
    {"username": "test.user", "dateOfBirth": "1990-05-15", "gender": "MALE"},
    {"username": "ayse123", "dateOfBirth": "1990-05-15", "gender": "FEMALE"},
    {"username": "mehmet456", "dateOfBirth": "1985-12-20", "gender": "MALE"},
    {"username": "fatma789", "dateOfBirth": "1995-08-01", "gender": "FEMALE"},
    {"username": "ali012", "dateOfBirth": "1980-03-10", "gender": "MALE"},
    {"username": "zeynep345", "dateOfBirth": "2000-11-25", "gender": "FEMALE"},
]


def create_user(user_data):
    """Creates a user using the API."""
    try:
        response = requests.post(
            USER_API_URL, data=json.dumps(user_data), headers=JSON_HEADER
        )
        response.raise_for_status()
        print(f"User {user_data['username']} created successfully.")
        return user_data
    except requests.exceptions.RequestException as e:
        print(f"Error creating user {user_data['username']}: {e}")


def create_users():
    users = []
    for user in USERS:
        users.append(create_user(user))
    return users


if __name__ == "__main__":
    create_users()
