<template>
  <a-modal v-model="show" title="新增餐品" @cancel="onClose" :width="800">
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
        <a-col :span="8">
          <a-form-item label='餐品名称' v-bind="formItemLayout">
            <a-input v-decorator="[
            'name',
            { rules: [{ required: true, message: '请输入名称!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label='餐品价格（元/100g）' v-bind="formItemLayout">
            <a-input-number style="width: 100%" :min="0.1" v-decorator="[
            'price',
            { rules: [{ required: true, message: '请输入餐品价格!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label='餐品类型' v-bind="formItemLayout">
            <a-select
              style="width: 100%"
              v-decorator="['type',{rules: [{ required: true, message: '请选择餐品类型' }]}]">
              <a-select-option v-for="(item, index) in typeList" :key="index" :value="item.id">{{item.typeName}}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label='餐品状态' v-bind="formItemLayout">
            <a-radio-group default-value="1" button-style="solid"
              v-decorator="[
              'onPut',
              { rules: [{ required: true, message: '请输入餐品状态!' }] }
              ]">
              <a-radio-button value="0">下架</a-radio-button>
              <a-radio-button value="1">上架</a-radio-button>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label='备注' v-bind="formItemLayout">
            <a-textarea :rows="6" v-decorator="[
            'content',
             { rules: [{ required: true, message: '请输入餐品备注!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="8">
        <a-form-item label='热量（千卡）' v-bind="formItemLayout">
          <a-input-number style="width: 100%" :min="0" v-decorator="[
            'calories',
            { rules: [{ required: false, message: '请输入热量!' }] }
            ]"/>
        </a-form-item>
      </a-col>
        <a-col :span="8">
          <a-form-item label='蛋白质含量（克/100g）' v-bind="formItemLayout">
            <a-input-number style="width: 100%" :min="0" :step="0.1" v-decorator="[
            'protein',
            { rules: [{ required: false, message: '请输入蛋白质含量!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label='脂肪含量（克/100g）' v-bind="formItemLayout">
            <a-input-number style="width: 100%" :min="0" :step="0.1" v-decorator="[
            'fat',
            { rules: [{ required: false, message: '请输入脂肪含量!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label='碳水化合物含量（克/100g）' v-bind="formItemLayout">
            <a-input-number style="width: 100%" :min="0" :step="0.1" v-decorator="[
            'carbohydrate',
            { rules: [{ required: false, message: '请输入碳水化合物含量!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label='钠含量（毫克/100g）' v-bind="formItemLayout">
            <a-input-number style="width: 100%" :min="0" :step="0.1" v-decorator="[
            'sodium',
            { rules: [{ required: false, message: '请输入钠含量!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label='膳食纤维含量（克/100g）' v-bind="formItemLayout">
            <a-input-number style="width: 100%" :min="0" :step="0.1" v-decorator="[
            'fiber',
            { rules: [{ required: false, message: '请输入膳食纤维含量!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label='糖含量（克/100g）' v-bind="formItemLayout">
            <a-input-number style="width: 100%" :min="0" :step="0.1" v-decorator="[
            'sugar',
            { rules: [{ required: false, message: '请输入糖含量!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="16">
          <a-form-item label='份量说明' v-bind="formItemLayout">
            <a-input v-decorator="[
            'servingSize',
            { rules: [{ required: false, message: '请输入份量说明!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label='过敏原信息' v-bind="formItemLayout">
            <a-textarea :rows="3" v-decorator="[
            'allergenInfo',
            { rules: [{ required: false, message: '请输入过敏原信息!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label='营养备注' v-bind="formItemLayout">
            <a-textarea :rows="3" v-decorator="[
            'nutritionRemark',
            { rules: [{ required: false, message: '请输入营养备注!' }] }
            ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label='图册' v-bind="formItemLayout">
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
  name: 'commodityAdd',
  props: {
    commodityAddVisiable: {
      default: false
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    }),
    show: {
      get: function () {
        return this.commodityAddVisiable
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
      previewImage: '',
      typeList: []
    }
  },
  mounted () {
    this.selectTypeList()
  },
  methods: {
    selectTypeList () {
      this.$get('/cos/commodity-type/list').then((r) => {
        this.typeList = r.data.data
      })
    },
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
        values.shopId = this.currentUser.userId
        if (!err) {
          this.loading = true
          this.$post('/cos/commodity-info', {
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
