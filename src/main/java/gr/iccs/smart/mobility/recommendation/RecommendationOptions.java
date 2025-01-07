package gr.iccs.smart.mobility.recommendation;

public record RecommendationOptions(
        Boolean wholeMap,
        Boolean previewGraph,
        RecommendationRequestOptionsDTO requestOptions) {

}
