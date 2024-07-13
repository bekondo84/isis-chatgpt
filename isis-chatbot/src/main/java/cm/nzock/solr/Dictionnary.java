package cm.nzock.solr;

import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

@SolrDocument(collection = "chatbot")
public class Dictionnary {

    @Id
    @Indexed(name = "id", type = "string")
    private String id ;
    @Indexed(name = "value", type = "string")
    private String value;
    @Indexed(name = "type", type = "string")
    private String type;

    public Dictionnary() {
    }

    public Dictionnary(String id, String value, String type) {
        this.id = id;
        this.value = value;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
