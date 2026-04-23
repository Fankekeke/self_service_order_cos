<template>
  <a-modal v-model="show" title="新增原材料" @cancel="onClose" :width="800">
    <template slot="footer">
      <a-button key="back" @click="onClose">
        取消
      </a-button>
      <a-button key="submit" type="primary" :loading="loading" @click="handleSubmit">
        提交
      </a-button>
    </template>
    <a-form :form="form" layout="vertical">
      <a-row :gutter="20">
        <a-col :span="12">
          <a-form-item label='原材料名称' v-bind="formItemLayout">
            <a-input v-decorator="[
            'name',
            { rules: [{ required: true, message: '请输入原材料名称!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='原材料分类' v-bind="formItemLayout">
            <a-select v-decorator="[
            'category',
            { rules: [{ required: true, message: '请选择原材料分类!' }] }
            ]" placeholder="请选择原材料分类">
              <a-select-option value="蔬菜类">蔬菜类</a-select-option>
              <a-select-option value="肉类">肉类</a-select-option>
              <a-select-option value="海鲜类">海鲜类</a-select-option>
              <a-select-option value="水果类">水果类</a-select-option>
              <a-select-option value="粮油类">粮油类</a-select-option>
              <a-select-option value="调味品类">调味品类</a-select-option>
              <a-select-option value="饮品类">饮品类</a-select-option>
              <a-select-option value="乳制品">乳制品</a-select-option>
              <a-select-option value="蛋类">蛋类</a-select-option>
              <a-select-option value="其他">其他</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='单位' v-bind="formItemLayout">
            <a-input v-decorator="[
            'unit',
            { rules: [{ required: true, message: '请输入单位!' }] }
            ]" placeholder="如：kg、g、L、ml等"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='规格型号' v-bind="formItemLayout">
            <a-input v-decorator="[
            'specification'
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='供应商' v-bind="formItemLayout">
            <a-input v-decorator="[
            'supplier'
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='单价' v-bind="formItemLayout">
            <a-input-number
              v-decorator="[
              'unitPrice',
              { rules: [{ required: true, message: '请输入单价!' }] }
              ]"
              :min="0"
              :precision="2"              style="width: 100%"
              placeholder="请输入单价"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='保质期(天)' v-bind="formItemLayout">
            <a-input-number
              v-decorator="[
              'shelfLife'
              ]"
              :min="0"              style="width: 100%"
              placeholder="请输入保质期天数"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='储存条件' v-bind="formItemLayout">
            <a-input v-decorator="[
            'storageCondition'
            ]" placeholder="如：常温、冷藏、冷冻等"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label='详细描述' v-bind="formItemLayout">
            <a-textarea :rows="6" v-decorator="[
            'description'
            ]" placeholder="请输入原材料详细描述"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label='图片' v-bind="formItemLayout">
            <a-upload
              name="avatar"
              action="http://127.0.0.1:9527/file/fileUpload/"
              list-type="picture-card"
              :file-list="fileList"
              @preview="handlePreview"
              @change="picHandleChange"
            >
              <div v-if="fileList.length < 8">
                <a-icon type="plus" />
                <div class="ant-upload-text">
                  Upload
                </div>
              </div>
            </a-upload>
            <a-modal :visible="previewVisible" :footer="null" @cancel="handleCancel">
              <img alt="example" style="width: 100%" :src="previewImage" />
            </a-modal>
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>

<script>
import {mapState} from 'vuex'
function getBase64 (file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}
const formItemLayout = {
  labelCol: { span: 24 },
  wrapperCol: { span: 24 }
}
export default {
  name: 'BulletinAdd',
  props: {
    bulletinAddVisiable: {
      default: false
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    }),
    show: {
      get: function () {
        return this.bulletinAddVisiable
      },
      set: function () {
      }
    }
  },
  data () {
    return {
      formItemLayout,
      form: this.$form.createForm(this),
      loading: false,
      fileList: [],
      previewVisible: false,
      previewImage: ''
    }
  },
  methods: {
    handleCancel () {
      this.previewVisible = false
    },
    async handlePreview (file) {
      if (!file.url && !file.preview) {
        file.preview = await getBase64(file.originFileObj)
      }
      this.previewImage = file.url || file.preview
      this.previewVisible = true
    },
    picHandleChange ({ fileList }) {
      this.fileList = fileList
    },
    reset () {
      this.loading = false
      this.form.resetFields()
    },
    onClose () {
      this.reset()
      this.$emit('close')
    },
    handleSubmit () {
      // 获取图片List
      let images = []
      this.fileList.forEach(image => {
        images.push(image.response)
      })
      this.form.validateFields((err, values) => {
        values.images = images.length > 0 ? images.join(',') : null
        if (!err) {
          this.loading = true
          this.$post('/cos/material-info', {
            ...values
          }).then((r) => {
            this.reset()
            this.$emit('success')
          }).catch(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style scoped>

</style>
