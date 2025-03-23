<template>
  <div class="picture-upload">
    <a-upload
      list-type="picture-card"
      :show-upload-list="false"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"
    >
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
      <div v-else>
        <loading-outlined v-if="loading"></loading-outlined>
        <plus-outlined v-else></plus-outlined>
        <div class="ant-upload-text">上传图片或拖拽</div>
      </div>
    </a-upload>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import type { UploadChangeParam, UploadProps } from 'ant-design-vue';
import { uploadPictureUsingPost } from '@/api/pictureController.ts'
import ImageCropper from '@/components/ImageCropper.vue'

const loading = ref<boolean>(false);

interface Props {
  picture?: API.PictureVO,
  spaceId?: number,
  onSuccess?: (newPicture: API.PictureVO) => void
}
const props = defineProps<Props>()


/**
 * 上传
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {};
    params.spaceId = props.spaceId;
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    message.error('图片上传失败')
  } finally {
    loading.value = false
  }
}


/**
 * 上传图片前对图片校验
 * @param file
 */
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  //1.校验图片格式
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('不支持上传该格式的图片，推荐jpg或png');
  }
  //2.校验图片大小
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('上传图片大小不能超过2MB!');
  }
  return isJpgOrPng && isLt2M;
};
</script>
<style scoped>
.picture-upload :deep(.ant-upload){
  width: 100% !important;
  height: 100% !important;
  min-height: 152px;
  min-width: 152px;
}
.picture-upload img{
  max-width: 100%;
  max-height: 480px;
}
.avatar-uploader > .ant-upload {
  width: 128px;
  height: 128px;
}
.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}
</style>
