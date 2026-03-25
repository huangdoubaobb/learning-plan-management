import { ref } from 'vue'

export const useSingleImageUpload = ({ api, message, uploadPath, fieldName = 'file' }) => {
  const imageInput = ref(null)
  const imageFile = ref(null)
  const imagePreview = ref(null)

  const clearPreview = () => {
    if (imagePreview.value?.url) {
      URL.revokeObjectURL(imagePreview.value.url)
    }
    imagePreview.value = null
  }

  const resetImageState = () => {
    clearPreview()
    imageFile.value = null
    if (imageInput.value) {
      imageInput.value.value = ''
    }
  }

  const triggerImagePick = () => {
    if (imageInput.value) {
      imageInput.value.value = ''
      imageInput.value.click()
    }
  }

  const handleImageChange = (event) => {
    const file = event?.target?.files?.[0]
    if (!file) return

    if (!file.type || !file.type.startsWith('image/')) {
      message.warning('仅支持图片文件')
      return
    }
    if (file.size > 10 * 1024 * 1024) {
      message.warning('图片大小不能超过 10MB')
      return
    }

    clearPreview()
    imageFile.value = file
    imagePreview.value = {
      url: URL.createObjectURL(file),
      name: file.name
    }
  }

  const removeImage = () => {
    resetImageState()
  }

  const uploadImage = async (id) => {
    if (!id || !imageFile.value) return

    const formData = new FormData()
    formData.append(fieldName, imageFile.value)

    await api.post(uploadPath(id), formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }

  return {
    imageInput,
    imageFile,
    imagePreview,
    resetImageState,
    triggerImagePick,
    handleImageChange,
    removeImage,
    uploadImage
  }
}
