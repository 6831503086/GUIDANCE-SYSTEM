
package guidance_system.service;

import guidance_system.entity.GuidanceTeacher;
import guidance_system.repository.GuidanceTeacherRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

@Service
public class ProfilePictureService {

    private final GuidanceTeacherRepository repository;

    private final Path uploadDirectory =
            Paths.get("uploads/profile-pictures")
                    .toAbsolutePath()
                    .normalize();

    public ProfilePictureService(
            GuidanceTeacherRepository repository) {
        this.repository = repository;
    }

    public String uploadPicture(
            GuidanceTeacher teacher,
            MultipartFile file) throws IOException {

        // Check empty file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select an image.");
        }

        // Check file size (maximum 2 MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Image size must not exceed 2 MB.");
        }

        // Check MIME type
        String contentType = file.getContentType();

        String extension;

        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            extension = ".jpg";
        } else if ("image/png".equalsIgnoreCase(contentType)) {
            extension = ".png";
        } else {
            throw new IllegalArgumentException(
                    "Only JPG and PNG images are allowed.");
        }

        // Validate actual image content
        try {
            BufferedImage image = ImageIO.read(
                    new ByteArrayInputStream(file.getBytes()));

            if (image == null) {
                throw new IllegalArgumentException(
                        "The uploaded file is not a valid image.");
            }

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Unable to read the uploaded image.");
        }

        // Create upload directory
        Files.createDirectories(uploadDirectory);

        // Generate unique filename
        String filename = UUID.randomUUID() + extension;

        Path target = uploadDirectory.resolve(filename).normalize();

        if (!target.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "Invalid file path.");
        }

        // Save uploaded image
        file.transferTo(target);

        // Update teacher profile picture
        String oldPicture = teacher.getProfilePicture();

        teacher.setProfilePicture(
                "/uploads/profile-pictures/" + filename);

        repository.save(teacher);

        // Delete previous profile picture
        if (oldPicture != null && !oldPicture.isBlank()) {

            String oldFilename = Paths.get(oldPicture)
                    .getFileName().toString();

            Files.deleteIfExists(
                    uploadDirectory.resolve(oldFilename));
        }

        return teacher.getProfilePicture();
    }
}
