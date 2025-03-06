<template>
  <div class="url-picture-upload">
    <a-input-group compact style="margin-bottom: 16px">
      <a-input v-model:value="fileUrl" style="width: calc(100% - 120px)" placeholder="请输入图片 URL" />
      <a-button type="primary" :loading="loading" @click="handleUpload" style="width: 120px">提交</a-button>
    </a-input-group>
    <div  class="imag-wrapper">
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { message } from 'ant-design-vue';
import type { UploadChangeParam, UploadProps } from 'ant-design-vue';
import { uploadPictureByUrlUsingPost, uploadPictureUsingPost } from '@/api/pictureController.ts'

const loading = ref<boolean>(false);
const fileUrl = ref<string>();

interface Props {
  picture?: API.PictureVO
  onSuccess?: (newPicture: API.PictureVO) => void
}
const props = defineProps<Props>()


/**
 * 上传
 * @param file
 */
const handleUpload = async () => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = {fileUrl:fileUrl.value}
    if(props.picture){
        params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
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
.url-picture-upload {
  width: 100% !important;
  height: 100% !important;
  min-height: 152px;
  min-width: 152px;
}
.url-picture-upload img{
  max-width: 100%;
  max-height: 480px;
}
.url-picture-upload .imag-wrapper{
  text-align: center;
  margin-top: 16px;
}
</style>
