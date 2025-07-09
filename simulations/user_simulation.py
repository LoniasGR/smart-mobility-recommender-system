import requests
import json
import random
import time
from datetime import date, timedelta

# API Endpoints
VEHICLE_API_URL = "http://your-api-endpoint/api/vehicles/create"  # Replace with your actual API endpoint
USER_API_URL = (
    "http://your-api-endpoint/api/users/create"  # Replace with your actual API endpoint
)
RIDE_API_URL = "http://your-api-endpoint/single-ride/{username}"  # Replace with your actual API endpoint

# Predefined List of Users
USERS = [
    {"username": "ayse123", "dateOfBirth": "1990-05-15", "gender": "female"},
    {"username": "mehmet456", "dateOfBirth": "1985-12-20", "gender": "male"},
    {"username": "fatma789", "dateOfBirth": "1995-08-01", "gender": "female"},
    {"username": "ali012", "dateOfBirth": "1980-03-10", "gender": "male"},
    {"username": "zeynep345", "dateOfBirth": "2000-11-25", "gender": "female"},
]

# Areas and Coordinates (Approximate - adjust based on your map data)
AREAS = {
    "Besiktas": {"latitude_range": (41.03, 41.06), "longitude_range": (29.00, 29.04)},
    "Kadikoy": {"latitude_range": (40.98, 41.01), "longitude_range": (29.02, 29.06)},
    "Adalar": {"latitude_range": (40.85, 40.92), "longitude_range": (29.07, 29.15)},
}

# Simulation Parameters
SIMULATION_DURATION = 60  # Seconds
USAGE_PROBABILITY = 0.2  # Probability of a vehicle being used in each iteration
MAX_TRIP_DISTANCE = 5  # Maximum trip distance in kilometers

# In-memory vehicle fleet (populated after initial placement)
vehicle_fleet = []


def create_user(user_data):
    """Creates a user using the API."""
    headers = {"Content-type": "application/json"}
    try:
        response = requests.post(
            USER_API_URL, data=json.dumps(user_data), headers=headers
        )
        response.raise_for_status()
        print(f"User {user_data['username']} created successfully.")
    except requests.exceptions.RequestException as e:
        print(f"Error creating user {user_data['username']}: {e}")


def get_all_vehicles():
    """Retrieves all vehicles from the API (replace with your actual API call)."""
    # This is a placeholder - you'll need to adapt this to your API.
    # Assuming you have an endpoint to get all vehicles:
    try:
        response = requests.get(
            "http://your-api-endpoint/api/vehicles"
        )  # Replace with your actual API endpoint
        response.raise_for_status()
        return response.json()  # Assuming the API returns a JSON list of vehicles
    except requests.exceptions.RequestException as e:
        print(f"Error retrieving vehicles: {e}")
        return []


def simulate_ride(vehicle, user):
    """Simulates a user taking a ride on a vehicle."""
    area_name = random.choice(list(AREAS.keys()))
    area = AREAS[area_name]
    destination_latitude = random.uniform(
        area["latitude_range"][0], area["latitude_range"][1]
    )
    destination_longitude = random.uniform(
        area["longitude_range"][0], area["longitude_range"][1]
    )

    payload = {
        "vehicleId": vehicle["id"],
        "destination": {
            "latitude": destination_latitude,
            "longitude": destination_longitude,
        },
    }
    headers = {"Content-type": "application/json"}
    ride_url = RIDE_API_URL.format(username=user["username"])

    try:
        response = requests.post(ride_url, data=json.dumps(payload), headers=headers)
        response.raise_for_status()
        print(
            f"User {user['username']} took vehicle {vehicle['id']} to ({destination_latitude:.4f}, {destination_longitude:.4f})"
        )
    except requests.exceptions.RequestException as e:
        print(
            f"Error simulating ride for user {user['username']} and vehicle {vehicle['id']}: {e}"
        )


def run_simulation():
    """Runs the vehicle usage simulation."""

    # 1. Create Users
    for user_data in USERS:
        create_user(user_data)

    # 2. Get the vehicle fleet from the API
    global vehicle_fleet
    vehicle_fleet = get_all_vehicles()

    if not vehicle_fleet:
        print(
            "No vehicles found in the fleet.  Make sure you've placed vehicles first."
        )
        return

    # 3. Run the simulation loop
    start_time = time.time()
    while time.time() - start_time < SIMULATION_DURATION:
        for vehicle in vehicle_fleet:
            if random.random() < USAGE_PROBABILITY:
                user = random.choice(USERS)
                simulate_ride(vehicle, user)
        time.sleep(1)  # Simulate time passing


if __name__ == "__main__":
    run_simulation()
