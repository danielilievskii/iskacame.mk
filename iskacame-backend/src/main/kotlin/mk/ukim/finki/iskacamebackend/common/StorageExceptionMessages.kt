package mk.ukim.finki.iskacamebackend.common

/**
 * Definitions for storage exception error messages.
 */
object StorageExceptionMessages {

  const val IMAGE_EMPTY = "File is empty."
  const val IMAGE_TOO_LARGE = "File size exceeds maximum allowed size."
  const val IMAGE_UNSUPPORTED_TYPE = "Invalid file type. Allowed types are JPEG, JPG, PNG, WEBP."
  const val IMAGE_INVALID = "File is not a valid image."

  const val UPLOAD_FAILED = "Failed to upload file to storage service. Please try again later."
  const val DELETE_FAILED = "Failed to delete file from storage service. Please try again later."
}