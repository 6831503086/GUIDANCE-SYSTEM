package guidance_system.dto;

public class ResultDatasetSummaryResponse {

    private long totalDatasets;
    private long publishedDatasets;
    private long pendingReviewDatasets;
    private long erroneousRecords;

    public ResultDatasetSummaryResponse(
            long totalDatasets,
            long publishedDatasets,
            long pendingReviewDatasets,
            long erroneousRecords) {

        this.totalDatasets = totalDatasets;
        this.publishedDatasets = publishedDatasets;
        this.pendingReviewDatasets = pendingReviewDatasets;
        this.erroneousRecords = erroneousRecords;
    }

    public long getTotalDatasets() {
        return totalDatasets;
    }

    public long getPublishedDatasets() {
        return publishedDatasets;
    }

    public long getPendingReviewDatasets() {
        return pendingReviewDatasets;
    }

    public long getErroneousRecords() {
        return erroneousRecords;
    }
}