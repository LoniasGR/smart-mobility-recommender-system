package gr.iccs.smart.mobility.openrouteservice;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DirectionsOptions {

    public class AlternativeRoutes {

        /**
         * Target number of alternative routes to compute. Service returns up to this
         * number of routes that fulfill the share-factor and weight-factor constraints.
         */
        @JsonProperty("target_count")
        private Integer targetCount;

        /**
         * Maximum factor by which route weight may diverge from the optimal route. The
         * default value of 1.4 means alternatives can be up to 1.4 times longer
         * (costly) than the optimal route.
         */
        @JsonProperty("weight_factor")
        private Integer weightFactor;

        /**
         * Maximum fraction of the route that alternatives may share with the optimal
         * route. The default value of 0.6 means alternatives can share up to 60% of
         * path segments with the optimal route.
         */
        @JsonProperty("share_factor")
        private Integer shareFactor;

        public Integer getTargetCount() {
            return targetCount;
        }

        public void setTargetCount(Integer targetCount) {
            this.targetCount = targetCount;
        }

        public Integer getWeightFactor() {
            return weightFactor;
        }

        public void setWeightFactor(Integer weightFactor) {
            this.weightFactor = weightFactor;
        }

        public Integer getShareFactor() {
            return shareFactor;
        }

        public void setShareFactor(Integer shareFactor) {
            this.shareFactor = shareFactor;
        }
    }

    public enum Attributes {
        avgspeed,
        detourfactor,
        percentage
    }

    public enum ExtraInfo {
        steepness,
        suitability,
        surface,
        waycategory,
        waytype,
        tollways,
        traildifficulty,
        osmid,
        roadaccessrestrictions,
        countryinfo,
        green,
        noise,
        csv,
        shadow
    }

    public enum InstructionsFormat {
        text,
        html
    }

    /**
     * The waypoints to use for the route as an array of longitude/latitude pairs in
     * WGS 84 (EPSG:4326)
     */
    private List<List<Double>> coordinates;

    /**
     * Specifies whether alternative routes are computed, and parameters for the
     * algorithm determining suitable alternatives.
     */
    @JsonProperty("alternative_routes")
    private AlternativeRoutes alternativeRoutes;

    /**
     * List of route attributes
     */
    private List<Attributes> attributes;

    /**
     * Forces the route to keep going straight at waypoints restricting uturns there
     * even if it would be faster.
     */
    @JsonProperty("continue_straight")
    private Boolean continueStraight;

    /**
     * Specifies whether to return elevation values for points. Please note that
     * elevation also gets encoded for json response encoded polyline.
     */
    private Boolean elevation;

    /**
     * The extra info items to include in the response
     */
    @JsonProperty("extra_info")
    private List<ExtraInfo> extraInfo;

    /**
     * Specifies whether to simplify the geometry. Simplify geometry cannot be
     * applied to routes with more than one segment and when extra_info is required.
     */
    private Boolean geometrySimplify;

    /**
     * Arbitrary identification string of the request reflected in the meta
     * information.
     */
    private String id;

    /**
     * Specifies whether to return instructions.
     */
    private Boolean instructions;

    /**
     * Select html for more verbose instructions.
     */
    @JsonProperty("instructions_format")
    private InstructionsFormat instructionsFormat;

    /**
     * TODO: Add all languages as enum
     * Language for the route instructions.
     */
    private String language;

    /**
     * Specifies whether the maneuver object is included into the step object or
     * not.
     */
    private Boolean maneuvers;

    // TODO: Add other options

    /**
     * The maximum speed specified by user.
     */
    @JsonProperty("maximum_speed")
    private Double maximumSpeed;

    /***************************************
     * GETTERS & SETTERS
     **************************************/

    public DirectionsOptions(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    public AlternativeRoutes getAlternativeRoutes() {
        return alternativeRoutes;
    }

    public void setAlternativeRoutes(AlternativeRoutes alternativeRoutes) {
        this.alternativeRoutes = alternativeRoutes;
    }

    public List<Attributes> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attributes> attributes) {
        this.attributes = attributes;
    }

    public Boolean getContinueStraight() {
        return continueStraight;
    }

    public void setContinueStraight(Boolean continueStraight) {
        this.continueStraight = continueStraight;
    }

    public Boolean getElevation() {
        return elevation;
    }

    public void setElevation(Boolean elevation) {
        this.elevation = elevation;
    }

    public List<ExtraInfo> getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(List<ExtraInfo> extraInfo) {
        this.extraInfo = extraInfo;
    }

    public Boolean getGeometrySimplify() {
        return geometrySimplify;
    }

    public void setGeometrySimplify(Boolean geometrySimplify) {
        this.geometrySimplify = geometrySimplify;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getInstructions() {
        return instructions;
    }

    public void setInstructions(Boolean instructions) {
        this.instructions = instructions;
    }

    public InstructionsFormat getInstructionsFormat() {
        return instructionsFormat;
    }

    public void setInstructionsFormat(InstructionsFormat instructionsFormat) {
        this.instructionsFormat = instructionsFormat;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getManeuvers() {
        return maneuvers;
    }

    public void setManeuvers(Boolean maneuvers) {
        this.maneuvers = maneuvers;
    }

    public Double getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setMaximumSpeed(Double maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
    }
}
