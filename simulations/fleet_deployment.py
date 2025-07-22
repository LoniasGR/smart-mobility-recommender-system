import requests
import json
import random
import argparse

from json_to_geojson import extract_feature, create_geojson_collection

# API Endpoint
LOG = True
ROOT_URL = "http://localhost:8080"
VEHICLE_API_ENDPOINT = (
    f"{ROOT_URL}/api/vehicles/"  # Replace with your actual API endpoint
)
PORT_API_ENDPOINT = f"{ROOT_URL}/api/ports"

# Vehicle Types
VEHICLE_TYPES = ["SCOOTER", "CAR", "SEA_VESSEL"]

# Areas and Vehicle Counts (Based on initial estimates)
AREAS = {
    "tubitak": {
        "Tubitak Campus": {
            "latitude": 40.785556,
            "longitude": 29.449167,
            "car_count": 2,
            "scooter_count": 10,
            "boat_count": 0,
            "ports": [],
        }
    },
    "halic": {
        "Halic Shipyard": {
            "latitude": 41.03361,
            "longitude": 28.95750,
            "car_count": 1,
            "scooter_count": 1,
            "boat_count": 5,
            "ports": [
                {
                    "name": "Halic Shipyard",
                    "latitude": 41.0400,
                    "longitude": 29.00745,
                }
            ],
        },
    },
    "full": {
        "Besiktas": {
            "latitude": 41.051008,
            "longitude": 29.00278,
            "scooter_count": 10,
            "car_count": 5,
            "boat_count": 2,
            "ports": [
                {
                    "name": "Beşiktaş Ferry Terminal",
                    "latitude": 41.04083,
                    "longitude": 29.00722,
                }
            ],
        },
        "Kadikoy": {
            "latitude": 40.9920,
            "longitude": 29.0270,
            "scooter_count": 30,
            "car_count": 2,
            "boat_count": 3,
            "ports": [
                {
                    "name": "Kadıköy Ferry Terminal",
                    "latitude": 40.9909,
                    "longitude": 29.0187,
                }
            ],
        },
        "Karaköy": {
            "latitude": 41.0257724,
            "longitude": 28.974084,
            "scooter_count": 10,
            "car_count": 7,
            "boat_count": 10,
            "ports": [
                {
                    "name": "Eminönü Şehir Hatları Ferry Pier",
                    "latitude": 41.016487,
                    "longitude": 28.970381,
                },
                {
                    "name": "Karaköy Şehir Hatları Ferry Pier",
                    "latitude": 41.016667,
                    "longitude": 28.966667,
                },
            ],
        },
    },
}


v_id = 1
p_id = 1


def log(payload):
    with open(FILE, "a") as f:
        f.write(f"{json.dumps(payload)},")


def print_centers(scenario="full"):
    features = []
    for area, config in AREAS[scenario].items():
        feature = extract_feature(config)
        feature["properties"]["name"] = area
        features.append(feature)
    with open(CENTERS, "w") as f:
        f.write(json.dumps(create_geojson_collection(features)))


def create_port(area_name, latitude, longitude):
    global p_id
    port_id = f"port-{p_id}"
    p_id = p_id + 1

    payload = {
        "port_id": port_id,
        "name": area_name,
        "location": {"latitude": latitude, "longitude": longitude},
    }
    headers = {"Content-type": "application/json"}
    try:
        response = requests.post(
            PORT_API_ENDPOINT, data=json.dumps(payload), headers=headers
        )
        response.raise_for_status()  # Raise HTTPError for bad responses (4xx or 5xx)
        print(f"Port {port_id} created successfully.")
    except requests.exceptions.RequestException as e:
        print(f"Error creating port {port_id}: {e}")


def create_vehicle(vehicle_type, latitude, longitude):
    global v_id
    vehicle_id = f"{vehicle_type.lower()}-{v_id}"
    v_id = v_id + 1
    payload = {
        "id": vehicle_id,
        "type": vehicle_type,
        "dummy": False,
        "location": {"latitude": latitude, "longitude": longitude},
        "battery": 100,  # Initial battery level
    }
    if LOG:
        log(payload)
    headers = {"Content-type": "application/json"}
    try:
        response = requests.post(
            VEHICLE_API_ENDPOINT, data=json.dumps(payload), headers=headers
        )
        response.raise_for_status()  # Raise HTTPError for bad responses (4xx or 5xx)
        print(f"Vehicle {vehicle_id} created successfully.")
    except requests.exceptions.RequestException as e:
        print(f"Error creating vehicle {vehicle_id}: {e}")


def deploy_fleet(scenario="full", scale=1):
    for area, config in AREAS[scenario].items():
        print(f"Creating port in {area}")
        for port in config["ports"]:
            create_port(port["name"], port["latitude"], port["longitude"])
        print(f"Deploying fleet in {area}...")
        # Scooters
        for _ in range(config["scooter_count"] * scale):
            # Randomize location slightly within the area
            latitude = config["latitude"] + random.uniform(-0.005, 0.005)
            longitude = config["longitude"] + random.uniform(-0.005, 0.005)
            create_vehicle("SCOOTER", latitude, longitude)
        # Cars
        for _ in range(config["car_count"] * scale):
            # Randomize location slightly within the area
            latitude = config["latitude"] + random.uniform(-0.005, 0.005)
            longitude = config["longitude"] + random.uniform(-0.005, 0.005)
            create_vehicle("CAR", latitude, longitude)
        # Boats
        for i in range(config["boat_count"] * scale):
            # Randomize location slightly within the area
            port = random.choice(config["ports"])

            print(port)
            latitude = port["latitude"]
            longitude = port["longitude"]
            create_vehicle("SEA_VESSEL", latitude, longitude)


def parse_args():
    parser = argparse.ArgumentParser(description="Multiply a number by 2.")
    parser.add_argument(
        "scenario",
        choices=["tubitak", "halic", "full"],
        default="full",
        help="the scenario to create",
    )
    parser.add_argument(
        "size",
        choices=["small", "medium", "big"],
        default="small",
        help="the total number of vehicles",
    )
    parser.add_argument("-o", "--output", type=str, default="vehicles.json")
    parser.add_argument("-c", "--centers-output", type=str, default="centers.json")
    args = parser.parse_args()

    global FILE
    FILE = args.output

    global CENTERS
    CENTERS = args.centers_output
    if args.size == "small":
        size = 100
    elif args.size == "medium":
        size = 1000
    else:
        size = 10000
    return args.scenario, size


if __name__ == "__main__":
    scenario, size = parse_args()
    print_centers(scenario)
    if LOG:
        with open(FILE, "w") as f:
            f.write("[")
    deploy_fleet(scenario, int(int(size) / 100))
    if LOG:
        with open(FILE, "rb+") as fh:
            fh.seek(-1, 2)  # Move cursor to the last byte
            fh.truncate()  # Remove everything from cursor (last byte) onward
        with open(FILE, "a") as f:
            f.write("]")
