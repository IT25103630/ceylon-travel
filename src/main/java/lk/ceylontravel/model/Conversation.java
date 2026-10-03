package lk.ceylontravel.model;

/**
 * Function: Tourist and Guide Chat Management
 * Member Name: Premasiriwardhana I. H. N. C.
 * Student ID: IT25103630
 * Role: Scrum Master
 * Course: SE2030 - Software Engineering (SLIIT)
 */
public class Conversation {
    private Long id;
    private Long touristId;
    private Long guideId;
    private String touristName;
    private String guideName;
    private String lastMessage;

    public Conversation() {}

    public Conversation(Long id, Long touristId, Long guideId, String touristName, String guideName, String lastMessage) {
        this.id = id;
        this.touristId = touristId;
        this.guideId = guideId;
        this.touristName = touristName;
        this.guideName = guideName;
        this.lastMessage = lastMessage;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTouristId() { return touristId; }
    public void setTouristId(Long touristId) { this.touristId = touristId; }

    public Long getGuideId() { return guideId; }
    public void setGuideId(Long guideId) { this.guideId = guideId; }

    public String getTouristName() { return touristName; }
    public void setTouristName(String touristName) { this.touristName = touristName; }

    public String getGuideName() { return guideName; }
    public void setGuideName(String guideName) { this.guideName = guideName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
}
