package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringImageDto
import org.springframework.web.multipart.MultipartFile

interface GatheringGalleryService {

    /**
     * Uploads multiple images to a gathering's gallery.
     *
     * @param gatheringId the ID of the gathering
     * @param files list of image files to upload
     * @return list of uploaded images as DTOs
     *
     * @throws ResourceNotFoundException if the gathering does not exist
     * @throws AccessDeniedException if the current user is not a participant of the gathering
     * @throws ImageValidationException if any file fails validation
     */
    fun uploadImage(gatheringId: Long, files: List<MultipartFile>): List<GatheringImageDto>

    /**
     * Deletes an image from a gathering's gallery.
     * Only the uploader of the image can delete it.
     *
     * @param gatheringId the ID of the gathering
     * @param imageId the ID of the image to delete
     *
     * @throws ResourceNotFoundException if the image does not exist
     * @throws AccessDeniedException if the current user is not the uploader of the image
     */
    fun deleteImage(gatheringId: Long, imageId: Long)

    /**
     * Retrieves all images for a gathering's gallery.
     *
     * @param gatheringId the ID of the gathering
     * @return list of images as DTOs
     *
     * @throws ResourceNotFoundException if the gathering does not exist
     * @throws AccessDeniedException if the current user is not a participant of the gathering
     */
    fun getGallery(gatheringId: Long): List<GatheringImageDto>
}