package org.grafana.downloader;

import java.util.Objects;

record GrafanaParameters(String domen, String dashboardId, String panelId, String orgId, String dataSource,
                         String measurement, String width, String height, String tz, String apiKey) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrafanaParameters that = (GrafanaParameters) o;
        return Objects.equals(domen, that.domen)
                && Objects.equals(dashboardId, that.dashboardId)
                && Objects.equals(panelId, that.panelId)
                && Objects.equals(orgId, that.orgId)
                && Objects.equals(dataSource, that.dataSource)
                && Objects.equals(measurement, that.measurement)
                && Objects.equals(width, that.width)
                && Objects.equals(height, that.height)
                && Objects.equals(tz, that.tz)
                && Objects.equals(apiKey, that.apiKey);
    }

    public String toString() {
        return "GrafanaParameters(domen=" + this.domen() + ", dashboardId=" + this.dashboardId() + ", panelId=" +
                this.panelId() + ", orgId=" + this.orgId() + ", dataSource=" + this.dataSource() +
                ", measurement=" + this.measurement() + ", width=" + this.width() + ", height=" + this.height()
                + ", tz=" + this.tz() + ", image=" + ", apiKey=" + this.apiKey() + ")";
    }
}
