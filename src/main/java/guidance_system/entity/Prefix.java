package guidance_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "prefix")
public class Prefix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prefix_id")
    private Long prefixId;

    @Column(name = "prefix_name", nullable = false)
    private String prefixName;

    public Prefix() {
    }

    public Long getPrefixId() {
        return prefixId;
    }

    public void setPrefixId(Long prefixId) {
        this.prefixId = prefixId;
    }

    public String getPrefixName() {
        return prefixName;
    }

    public void setPrefixName(String prefixName) {
        this.prefixName = prefixName;
    }
}