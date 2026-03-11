package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.GatheringExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringImageDto
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.GatheringImageMapper
import mk.ukim.finki.iskacamebackend.model.domain.GatheringImage
import mk.ukim.finki.iskacamebackend.repository.GatheringImageRepository
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.CloudinaryStorageService
import mk.ukim.finki.iskacamebackend.service.intf.GatheringGalleryService
import mk.ukim.finki.iskacamebackend.service.intf.GatheringService
import mk.ukim.finki.iskacamebackend.utils.ImageValidator
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

/**
 * Implementation of GatheringGalleryService
 */
@Service
class GatheringGalleryServiceImpl(
    private val gatheringService: GatheringService,
    private val gatheringImageRepository: GatheringImageRepository,
    private val authService: AuthService,
    private val cloudinaryStorageService: CloudinaryStorageService,
    private val gatheringImageMapper: GatheringImageMapper
) : GatheringGalleryService {

    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun uploadImage(gatheringId: Long, files: List<MultipartFile>): List<GatheringImageDto> {

        val gathering = gatheringService.getGatheringById(gatheringId)
        val currentUser = authService.getCurrentUser()

        files.forEach { ImageValidator.validate(it) }

        val images = files.map { file ->
            val publicId = generateGatheringImagePublicId(gatheringId, currentUser.id!!)
            val response = cloudinaryStorageService.uploadFile(file, publicId)

            GatheringImage(
                gathering = gathering,
                uploader = currentUser,
                url = response.url,
                publicId = response.publicId,
            )
        }

        return gatheringImageRepository.saveAll(images)
            .map(gatheringImageMapper::toGatheringImageDto)
    }

    @PreAuthorize("@permissionService.isGatheringImageOwner(#gatheringId, #imageId, authentication.principal.id)")
    override fun deleteImage(gatheringId: Long, imageId: Long) {

        val image = gatheringImageRepository.findById(imageId)
            .orElseThrow { ResourceNotFoundException(GatheringExceptionMessages.IMAGE_NOT_FOUND) }

        cloudinaryStorageService.deleteFile(image.publicId)
        gatheringImageRepository.delete(image)
    }

    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun getGallery(gatheringId: Long): List<GatheringImageDto> {

        return gatheringImageRepository.findAllByGatheringId(gatheringId)
            .map { gatheringImageMapper.toGatheringImageDto(it) }
    }

    private fun generateGatheringImagePublicId(gatheringId: Long, userId: Long): String =
        "gatherings/$gatheringId/images/${Instant.now().epochSecond}_${userId}"
}