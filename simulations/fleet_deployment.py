import requests
import json
import random

# API Endpoint
API_ENDPOINT = "http://your-api-endpoint/api/vehicles/create"  # Replace with your actual API endpoint

# Vehicle Types
VEHICLE_TYPES = ["SCOOTER", "CAR", "BOAT"]

# Areas and Vehicle Counts (Based on initial estimates)
AREAS = {
    "Besiktas": {
        "latitude": 41.0400,
        "longitude": 29.0080,
        "scooter_count": 60,
        "car_count": 12,
        "boat_count": 2,
    },
    "Kadikoy": {
        "latitude": 40.9920,
        "longitude": 29.0270,
        "scooter_count": 120,
        "car_count": 25,
        "boat_count": 3,
    },
    "Adalar": {
        "latitude": 40.8760,
        "longitude": 29.1230,
        "scooter_count": 40,
        "car_count": 7,
        "boat_count": 2,
    },
}


def create_vehicle(vehicle_type, latitude, longitude):
    vehicle_id = f"{vehicle_type.lower()}-{random.randint(1000, 9999)}"
    payload = {
        "id": vehicle_id,
        "type": vehicle_type,
        "dummy": False,
        "location": {"latitude": latitude, "longitude": longitude},
        "battery": 100,  # Initial battery level
    }
    headers = {"Content-type": "application/json"}
    try:
        response = requests.post(
            API_ENDPOINT, data=json.dumps(payload), headers=headers
        )
        response.raise_for_status()  # Raise HTTPError for bad responses (4xx or 5xx)
        print(f"Vehicle {vehicle_id} created successfully.")
    except requests.exceptions.RequestException as e:
        print(f"Error creating vehicle {vehicle_id}: {e}")


def deploy_fleet():
    for area, config in AREAS.items():
        print(f"Deploying fleet in {area}...")
        # Scooters
        for _ in range(config["scooter_count"]):
            # Randomize location slightly within the area
            latitude = config["latitude"] + random.uniform(-0.005, 0.005)
            longitude = config["longitude"] + random.uniform(-0.005, 0.005)
            create_vehicle("SCOOTER", latitude, longitude)
        # Cars
        for _ in range(config["car_count"]):
            # Randomize location slightly within the area
            latitude = config["latitude"] + random.uniform(-0.005, 0.005)
            longitude = config["longitude"] + random.uniform(-0.005, 0.005)
            create_vehicle("CAR", latitude, longitude)
        # Boats
        for _ in range(config["boat_count"]):
            # Randomize location slightly within the area
            latitude = config["latitude"] + random.uniform(-0.005, 0.005)
            longitude = config["longitude"] + random.uniform(-0.005, 0.005)
            create_vehicle("BOAT", latitude, longitude)


if __name__ == "__main__":
    deploy_fleet()
