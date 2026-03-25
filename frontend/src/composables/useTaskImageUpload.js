import { ref } from 'vue'
import { useMultiImageUpload } from './useMultiImageUpload'

export const useTaskImageUpload = ({ api, message }) => {
  const existingTaskImages = ref([])
  const {
    imageInput: taskImageInput,
    imageFiles: taskImageFiles,
    imagePreviews: taskImagePreviews,
    resetImageState: resetTaskImageState,
    triggerImagePick: triggerTaskImagePick,
    handleImageChange: handleTaskImageChange,
    removeImage: removeTaskImage,
    uploadImages: uploadTaskImages
  } = useMultiImageUpload({
    api,
    message,
    uploadPath: (taskId) => `/parent/tasks/${taskId}/images`
  })

  const setExistingTaskImages = (images) => {
    existingTaskImages.value = Array.isArray(images) ? images : []
  }

  const clearExistingTaskImages = () => {
    existingTaskImages.value = []
  }

  return {
    taskImageInput,
    taskImageFiles,
    taskImagePreviews,
    existingTaskImages,
    resetTaskImageState,
    triggerTaskImagePick,
    handleTaskImageChange,
    removeTaskImage,
    uploadTaskImages,
    setExistingTaskImages,
    clearExistingTaskImages
  }
}
