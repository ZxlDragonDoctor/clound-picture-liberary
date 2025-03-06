<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{route.query?.id ? '修改图片':'创建图片' }}
    </h2>
    <!-- 选择上传方式 -->
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="文件上传">
        <PictureUpload :picture="picture" :onSuccess="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="URL 上传" force-render>
        <UrlPictureUpload :picture="picture" :onSuccess="onSuccess" />
      </a-tab-pane>
    </a-tabs>
    <!--    图片信息表单-->
    <a-form  v-if="picture" layout="vertical" :model="pictureForm" @finish="handleSubmit">
      <a-form-item name="name" label="名称">
        <a-input v-model:value="pictureForm.name" placeholder="请输入名称" allow-clear />
      </a-form-item>
      <a-form-item name="introduction" label="简介">
        <a-textarea v-model:value="pictureForm.introduction" placeholder="请输入简介"
                    :auto-size="{minRows:2,maxRows:5}"/>
      </a-form-item>
      <a-form-item name="category" label="分类" >
        <a-auto-complete  v-model:value="pictureForm.category" :options="categoryOptions" placeholder="请输入分类" allow-clear />
      </a-form-item>
      <a-form-item name="tags" label="标签">
        <a-select  v-model:value="pictureForm.tags" :options="tagOptions" mode="tags" placeholder="请输入标签" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">创建</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 20px"></div>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { onMounted, reactive, ref } from 'vue'
import {
  editPictureUsingPost,
  getPictureByIdUsingGet, getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useRoute } from 'vue-router'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const uploadType = ref<'file' | 'url'>('file')

/**
 * 接受返回图片信息
 * @param newPicture
 */
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name  //展示上传图片名称
}

/**
 * 修改已上传的图片信息（即创建图片）
 * @param values
 */
const handleSubmit = async (values:any)=>{
  const pictureId = picture.value.id
  if(!pictureId){
    return
  }

  const res = await  editPictureUsingPost({
    id:pictureId,
    ...values
  })
  if(res.data.code===0&&res.data.data){
    message.success("上传图片成功")
  //   跳转图片详情页
    await router.push({
      path: `/picture/${pictureId}`
    })
  }else {
    message.error("创建图片失败"+res.data.message)
  }

}

const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接受的格式
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('加载选项失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions();
  getOldPicture();
})
const route = useRoute();
const getOldPicture = async ()=>{
  //获取数据
  const oldPictureId =  route.query?.id;
  if(oldPictureId) {
    const res = await getPictureVoByIdUsingGet({ id: oldPictureId });
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data;
      picture.value = data;
      pictureForm.name = data.name;
      pictureForm.introduction = data.introduction;
      pictureForm.category = data.category;
      pictureForm.tags = data.tags;
    }
  }
}
</script>

<style scoped>
#addPicturePage{
  max-width: 720px;
  margin: 0 auto;
}
</style>
