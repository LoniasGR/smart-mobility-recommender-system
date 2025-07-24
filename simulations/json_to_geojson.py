import argparse
import json


def extract_feature(obj, location_key=None):
    # Extract coordinates and properties
    tmp = obj.copy()
    location = tmp
    if location_key:
        location = tmp.pop(location_key)
    lat = location.pop("latitude")
    lon = location.pop("longitude")

    feature = {
        "type": "Feature",
        "geometry": {"type": "Point", "coordinates": [lon, lat]},
        "properties": tmp,
    }
    return feature


def create_geojson_collection(features):
    geojson = {"type": "FeatureCollection", "features": features}
    return geojson


def json_to_geojson(json_array, location_key=None):
    features = []
    for obj in json_array:
        feature = extract_feature(obj, location_key)
        features.append(feature)
    return create_geojson_collection(features)


def main():
    parser = argparse.ArgumentParser(
        description="Convert JSON array to GeoJSON FeatureCollection."
    )
    parser.add_argument("input_file", help="Path to the input JSON file")
    parser.add_argument("-o", "--output", help="Path to output GeoJSON file (optional)")
    args = parser.parse_args()

    # Load input file
    with open(args.input_file, "r", encoding="utf-8") as f:
        data = json.load(f)
    geojson = json_to_geojson(data, "location")

    # Output
    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            json.dump(geojson, f, indent=2)
        print(f"GeoJSON written to {args.output}")
    else:
        print(json.dumps(geojson, indent=2))


if __name__ == "__main__":
    main()
