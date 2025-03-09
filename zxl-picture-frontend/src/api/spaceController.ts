// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** addSpace POST /api/space/add */
export async function addSpaceUsingPost(
  body: API.SpaceAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong_>('/api/space/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** deleteSpace POST /api/space/delete */
export async function deleteSpaceUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/space/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editPicture POST /api/space/edit */
export async function editPictureUsingPost1(
  body: API.SpaceEditRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/space/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getSpaceVOById GET /api/space/get/vo */
export async function getSpaceVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceVOByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseSpaceVO_>('/api/space/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** listSpaceLevel GET /api/space/list/level */
export async function listSpaceLevelUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListSpaceLevel_>('/api/space/list/level', {
    method: 'GET',
    ...(options || {}),
  })
}

/** listSpaceVOByPage POST /api/space/list/page/vo */
export async function listSpaceVoByPageUsingPost(
  body: API.SpaceQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageSpaceVO_>('/api/space/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listSpaceByPage POST /api/space/list/space */
export async function listSpaceByPageUsingPost(
  body: API.SpaceQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageSpace_>('/api/space/list/space', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** updateSpace POST /api/space/update */
export async function updateSpaceUsingPost(
  body: API.SpaceUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/space/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
