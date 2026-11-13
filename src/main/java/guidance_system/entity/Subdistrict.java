package guidance_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subdistrict")
public class Subdistrict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subdistrict_id")
    private Long subdistrictId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "zipcode", length = 10)
    private String zipcode;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    public Subdistrict() {}

    public Long getSubdistrictId() {
        return subdistrictId;
    }

    public void setSubdistrictId(Long subdistrictId) {
        this.subdistrictId = subdistrictId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }
}