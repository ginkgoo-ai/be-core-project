public interface ProjectRoleStatistics {
    String getRoleId();
    Integer getTotal();
    Integer getAdded();
    Integer getSubmitted();
    Integer getShortlisted();
    Integer getDeclined();
}