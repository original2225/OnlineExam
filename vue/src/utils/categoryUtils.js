import request from '@/utils/request.js'

/**
 * 获取分类树并展平为列表，子分类显示为 "父分类 / 子分类"
 */
export function loadFlatCategories() {
  return request.get('/questionCategory/selectTree').then(res => {
    if (res.code === '200') {
      const flat = []
      const tree = res.data || []
      tree.forEach(parent => {
        flat.push({ id: parent.id, name: parent.name, fullName: parent.name })
        if (parent.children) {
          parent.children.forEach(child => {
            flat.push({
              id: child.id,
              name: child.name,
              fullName: parent.name + ' / ' + child.name
            })
          })
        }
      })
      return flat
    }
    return []
  })
}

/**
 * 获取 el-cascader 兼容的分类选项
 */
export function loadCascaderCategories() {
  return request.get('/questionCategory/selectTree').then(res => {
    if (res.code === '200') {
      const tree = res.data || []
      return tree.map(parent => ({
        value: parent.id,
        label: parent.name,
        children: (parent.children || []).map(child => ({
          value: child.id,
          label: child.name
        }))
      }))
    }
    return []
  })
}
