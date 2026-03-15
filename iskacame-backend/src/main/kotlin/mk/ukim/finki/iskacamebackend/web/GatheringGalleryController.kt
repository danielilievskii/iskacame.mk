package mk.ukim.finki.iskacamebackend.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringImageDto
import mk.ukim.finki.iskacamebackend.service.intf.GatheringGalleryService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/gatherings/{gatheringId}/gallery")
@Tag(name = "Gathering Gallery", description = "Manage images in a gathering's gallery")
class GatheringGalleryController(
    private val gatheringGalleryService: GatheringGalleryService
) {

    @GetMapping
    @Operation(summary = "Get all images in a gathering's gallery")
    fun getGallery(@PathVariable gatheringId: Long): ResponseEntity<List<GatheringImageDto>> {

        val images = gatheringGalleryService.getGallery(gatheringId)
        return ResponseEntity.ok(images)
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload images to a gathering's gallery")
    fun uploadImages(
        @PathVariable gatheringId: Long,
        @RequestParam("files") files: List<MultipartFile>
    ): ResponseEntity<List<GatheringImageDto>> {

        val images = gatheringGalleryService.uploadImage(gatheringId, files)
        return ResponseEntity.status(HttpStatus.CREATED).body(images)
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete an image from a gathering's gallery")
    fun deleteImage(
        @PathVariable gatheringId: Long,
        @PathVariable imageId: Long
    ): ResponseEntity<Void> {

        gatheringGalleryService.deleteImage(gatheringId, imageId)
        return ResponseEntity.noContent().build()
    }
}