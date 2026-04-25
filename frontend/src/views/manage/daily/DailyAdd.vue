<template>
  <a-modal v-model="show" title="新增供应计划" @cancel="onClose" :width="1000">
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
          <a-form-item label='日期' v-bind="formItemLayout">
            <a-date-picker
              v-decorator="[
              'date',
              { rules: [{ required: true, message: '请选择日期!' }] }
              ]"              style="width: 100%"
              placeholder="请选择供应日期"
              format="YYYY-MM-DD" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='备注' v-bind="formItemLayout">
            <a-input v-decorator="[
            'remark'
            ]" placeholder="请输入备注信息"/>
          </a-form-item>
        </a-col>
      </a-row>

      <a-divider>菜品供应计划</a-divider>

      <div class="daily-plan-container">
        <a-button type="primary" @click="addCommodityRow" style="margin-bottom: 16px">
          <a-icon type="plus" /> 添加菜品
        </a-button>

        <a-table
          :columns="planColumns"
          :dataSource="dailyPlanList"
          :pagination="false"
          :rowKey="(record, index) => index"
          size="small">
          <template slot="commodityIdSelect" slot-scope="text, record, index">
            <a-select
              v-model="record.commodityId"
              placeholder="请选择菜品"
              style="width: 100%"
              show-search
              :filter-option="filterCommodityOption">
              <a-select-option
                v-for="commodity in commodityList"
                :key="commodity.id"
                :value="commodity.id">
                {{ commodity.name }} ({{ commodity.code }})
              </a-select-option>
            </a-select>
          </template>
          <template slot="plannedQuantityInput" slot-scope="text, record, index">
            <a-input-number
              v-model="record.plannedQuantity"
              :min="0"              style="width: 100%"
              placeholder="计划供应量" />
          </template>
          <template slot="remarkInput" slot-scope="text, record, index">
            <a-input
              v-model="record.remark"
              placeholder="备注" />
          </template>
          <template slot="action" slot-scope="text, record, index">
            <a-icon
              type="delete"
              theme="twoTone"
              twoToneColor="#ff4d4f"
              @click="removeCommodityRow(index)"
              title="删除" />
          </template>
        </a-table>
      </div>
    </a-form>
  </a-modal>
</template>

<script>
import moment from 'moment'
import {mapState} from 'vuex'
moment.locale('zh-cn')
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
    },
    planColumns () {
      return [{
        title: '菜品',
        dataIndex: 'commodityId',
        width: 300,
        scopedSlots: { customRender: 'commodityIdSelect' }
      }, {
        title: '计划供应量',
        dataIndex: 'plannedQuantity',
        width: 150,
        scopedSlots: { customRender: 'plannedQuantityInput' }
      }, {
        title: '备注',
        dataIndex: 'remark',
        scopedSlots: { customRender: 'remarkInput' }
      }, {
        title: '操作',
        dataIndex: 'action',
        width: 80,
        scopedSlots: { customRender: 'action' }
      }]
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
      dailyPlanList: [],
      commodityList: []
    }
  },
  mounted () {
    this.loadCommodityList()
  },
  methods: {
    moment,
    loadCommodityList () {
      this.$get('/cos/commodity-info/list').then((r) => {
        this.commodityList = r.data.data || []
      })
    },
    addCommodityRow () {
      this.dailyPlanList.push({
        commodityId: undefined,
        plannedQuantity: 0,
        remark: ''
      })
    },
    removeCommodityRow (index) {
      this.dailyPlanList.splice(index, 1)
    },
    filterCommodityOption (input, option) {
      return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    },
    reset () {
      this.loading = false
      this.form.resetFields()
      this.dailyPlanList = []
    },
    onClose () {
      this.reset()
      this.$emit('close')
    },
    handleSubmit () {
      this.form.validateFields((err, values) => {
        if (!err) {
          // 验证至少添加一条菜品计划
          if (this.dailyPlanList.length === 0) {
            this.$message.warning('请至少添加一条菜品供应计划')
            return
          }

          // 验证所有菜品都已选择
          const invalidItem = this.dailyPlanList.find(item => !item.commodityId)
          if (invalidItem) {
            this.$message.warning('请选择所有菜品')
            return
          }

          this.loading = true

          // 格式化日期
          values.date = values.date ? moment(values.date).format('YYYY-MM-DD') : null

          // 将菜品计划列表转换为JSON字符串
          values.dailyPlanStr = JSON.stringify(this.dailyPlanList)

          this.$post('/cos/daily-supply-plan-detail', {
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
.daily-plan-container {
  max-height: 400px;
  overflow-y: auto;
}
</style>
