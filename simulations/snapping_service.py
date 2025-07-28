import traceback
import requests

from constants import OPENROUTESERVCE_API_URL


def snap_to_position(lat, lon, profile, radius=50):
    try:
        headers = {"Content-type": "application/json"}
        body = {
            "locations": [[lon, lat]],
            "radius": radius,
        }
        resp = requests.post(
            f"{OPENROUTESERVCE_API_URL}/{profile}", json=body, headers=headers
        )
        resp.raise_for_status()
        print(f"Snapping response: {resp.status_code} {resp.text}")
        if resp.json()["locations"][0] is None:
            return snap_to_position(lat, lon, profile, radius + 50)

        return resp.json()["locations"][0]["location"]
    except requests.exceptions.RequestException as e:
        print("".join(traceback.format_exception_only(e)).strip())
        return []


if __name__ == "__main__":
    # Example usage
    lat = 40.9920
    lon = 29.0270
    profile = "driving-car"
    snapped_location = snap_to_position(lat, lon, profile)
    print(snapped_location)
