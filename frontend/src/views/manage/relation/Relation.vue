<template>
  <a-card :bordered="false" class="card-area">
    <div :class="advanced ? 'search' : null">
      <!-- 搜索区域 -->
      <a-form layout="horizontal">
        <a-row :gutter="15">
          <div :class="advanced ? null: 'fold'">
            <a-col :md="6" :sm="24">
              <a-form-item
                label="餐品名称"
                :labelCol="{span: 5}"
                :wrapperCol="{span: 18, offset: 1}">
                <a-input v-model="queryParams.name"/>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item
                label="餐品原料"
                :labelCol="{span: 5}"
                :wrapperCol="{span: 18, offset: 1}">
                <a-input v-model="queryParams.model"/>
              </a-form-item>
            </a-col>
          </div>
          <span style="float: right; margin-top: 3px;">
            <a-button type="primary" @click="search">查询</a-button>
            <a-button style="margin-left: 8px" @click="reset">重置</a-button>
          </span>
        </a-row>
      </a-form>
    </div>
    <div>
      <div class="operator">
<!--        <a-button type="primary" ghost @click="add">新增</a-button>-->
<!--        <a-button @click="batchDelete">删除</a-button>-->
      </div>
      <!-- 表格区域 -->
      <a-table ref="TableInfo"
               :columns="columns"
               :rowKey="record => record.id"
               :dataSource="dataSource"
               :pagination="pagination"
               :loading="loading"
               :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
               :scroll="{ x: 900 }"
               @change="handleTableChange">
        <div slot="expandedRowRender" slot-scope="record" style="margin: 0">
          <div v-if="!record.materials || record.materials.length === 0" style="padding: 30px; text-align: center; color: #999; font-size: 14px; background: #fafafa; border-radius: 4px;">
            <a-icon type="inbox" style="font-size: 24px; margin-bottom: 8px; display: block;" />
            暂无原材料数据
          </div>
          <div v-else style="padding: 20px; background: #fafafa; border-radius: 4px;">
            <a-row :gutter="20">
              <a-col :span="6" v-for="(material, index) in record.materials" :key="index" style="margin-bottom: 16px;">
                <a-card
                  size="small"
                  :bordered="true"
                  :title="'原料 ' + (index + 1)"                  style="border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); transition: all 0.3s; background: #fff;">
                  <div style="line-height: 2.2; font-size: 13px;">
                    <p style="margin: 6px 0; padding: 4px 8px; background: linear-gradient(to right, #f0f5ff, transparent); border-left: 3px solid #1890ff; border-radius: 2px;">
                      <strong style="color: #1890ff; min-width: 70px; display: inline-block;">名称：</strong>
                      <span style="color: #262626; font-weight: 500;">{{ material.materialName || '- -' }}</span>
                    </p>
                    <p style="margin: 6px 0; padding: 4px 8px; background: linear-gradient(to right, #f6ffed, transparent); border-left: 3px solid #52c41a; border-radius: 2px;">
                      <strong style="color: #52c41a; min-width: 70px; display: inline-block;">编号：</strong>
                      <span style="color: #262626; font-family: monospace;">{{ material.materialCode || '- -' }}</span>
                    </p>
                    <p style="margin: 6px 0; padding: 4px 8px; background: linear-gradient(to right, #fff7e6, transparent); border-left: 3px solid #faad14; border-radius: 2px;">
                      <strong style="color: #faad14; min-width: 70px; display: inline-block;">分类：</strong>
                      <span style="color: #262626;">{{ material.category || '- -' }}</span>
                    </p>
                    <p style="margin: 6px 0; padding: 4px 8px; background: linear-gradient(to right, #f9f0ff, transparent); border-left: 3px solid #722ed1; border-radius: 2px;">
                      <strong style="color: #722ed1; min-width: 70px; display: inline-block;">规格：</strong>
                      <span style="color: #262626;">{{ material.specification || '- -' }}</span>
                    </p>
                    <p style="margin: 6px 0; padding: 4px 8px; background: linear-gradient(to right, #e6fffb, transparent); border-left: 3px solid #13c2c2; border-radius: 2px;">
                      <strong style="color: #13c2c2; min-width: 70px; display: inline-block;">用量：</strong>
                      <span style="color: #262626; font-weight: 600;">{{ material.quantity !== null && material.quantity !== undefined ? material.quantity + ' ' + (material.relationUnit || '') : '- -' }}</span>
                    </p>
                    <p style="margin: 6px 0; padding: 4px 8px; background: linear-gradient(to right, #fff1f0, transparent); border-left: 3px solid #ff4d4f; border-radius: 2px;">
                      <strong style="color: #ff4d4f; min-width: 70px; display: inline-block;">单价：</strong>
                      <span style="color: #ff4d4f; font-weight: 600; font-size: 14px;">{{ material.unitPrice !== null && material.unitPrice !== undefined ? material.unitPrice + '元' : '- -' }}</span>
                    </p>
                    <p style="margin: 6px 0; padding: 4px 8px; background: linear-gradient(to right, #f0f5ff, transparent); border-left: 3px solid #2f54eb; border-radius: 2px;">
                      <strong style="color: #2f54eb; min-width: 70px; display: inline-block;">供应商：</strong>
                      <span style="color: #262626;">{{ material.supplier || '- -' }}</span>
                    </p>
                    <p v-if="material.remark" style="margin: 6px 0; padding: 4px 8px; background: linear-gradient(to right, #fffbe6, transparent); border-left: 3px solid #fa8c16; border-radius: 2px;">
                      <strong style="color: #fa8c16; min-width: 70px; display: inline-block;">备注：</strong>
                      <span style="color: #595959; font-style: italic;">{{ material.remark }}</span>
                    </p>
                  </div>
                </a-card>
              </a-col>
            </a-row>
          </div>
        </div>
        <template slot="contentShow" slot-scope="text, record">
          <template>
            <a-tooltip>
              <template slot="title">
                {{ record.content }}
              </template>
              {{ record.content.slice(0, 10) }} ...
            </a-tooltip>
          </template>
        </template>
        <template slot="operation" slot-scope="text, record">
          <a-icon type="setting" theme="twoTone" twoToneColor="#4a9ff5" @click="edit(record)" title="修 改"></a-icon>
          <a-icon type="profile" theme="twoTone" twoToneColor="#4a9ff5" @click="view(record)" title="详 情" style="margin-left: 15px"></a-icon>
          <a-icon type="link" @click="openMaterialRelation(record)" title="原材料关联" style="margin-left: 15px"></a-icon>
        </template>
      </a-table>
    </div>

    <a-modal
      v-model="materialRelationModal.visible"
      title="原材料关联管理"
      @ok="handleMaterialRelationSubmit"
      @cancel="handleMaterialRelationClose"
      :width="1000"
      :confirmLoading="materialRelationModal.loading">
      <div class="material-relation-container">
        <a-button type="primary" @click="addMaterialRow" style="margin-bottom: 16px">
          <a-icon type="plus" /> 添加原材料
        </a-button>
        <a-table
          :columns="materialColumns"
          :dataSource="materialRelationModal.relationList"
          :pagination="false"
          :rowKey="(record, index) => index"
          size="small">
          <template slot="materialIdSelect" slot-scope="text, record, index">
            <a-select
              v-model="record.materialId"
              placeholder="请选择原材料"
              style="width: 100%"
              show-search
              :filter-option="filterMaterialOption"
              @change="(value) => handleMaterialChange(value, index)">
              <a-select-option
                v-for="material in materialList"
                :key="material.id"
                :value="material.id">
                {{ material.name }} ({{ material.category }})
              </a-select-option>
            </a-select>
          </template>
          <template slot="quantityInput" slot-scope="text, record, index">
            <a-input-number
              v-model="record.quantity"
              :min="0"
              :precision="2"              style="width: 100%"
              placeholder="数量" />
          </template>
          <template slot="unitInput" slot-scope="text, record, index">
            <a-input
              v-model="record.unit"
              placeholder="单位" />
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
              @click="removeMaterialRow(index)"
              title="删除" />
          </template>
        </a-table>
      </div>
    </a-modal>
  </a-card>
</template>

<script>
import RangeDate from '@/components/datetime/RangeDate'
import {mapState} from 'vuex'
import moment from 'moment'
moment.locale('zh-cn')

export default {
  name: 'commodity',
  components: {RangeDate},
  data () {
    return {
      advanced: false,
      commodityView: {
        visiable: false,
        data: null
      },
      commodityAdd: {
        visiable: false
      },
      commodityEdit: {
        visiable: false
      },
      materialRelationModal: {
        visible: false,
        loading: false,
        commodityId: null,
        relationList: []
      },
      materialList: [],
      queryParams: {},
      filteredInfo: null,
      sortedInfo: null,
      paginationInfo: null,
      dataSource: [],
      selectedRowKeys: [],
      loading: false,
      pagination: {
        pageSizeOptions: ['10', '20', '30', '40', '100'],
        defaultCurrent: 1,
        defaultPageSize: 10,
        showQuickJumper: true,
        showSizeChanger: true,
        showTotal: (total, range) => `显示 ${range[0]} ~ ${range[1]} 条记录，共 ${total} 条记录`
      },
      userList: []
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    }),
    columns () {
      return [{
        title: '餐品名称',
        ellipsis: true,
        dataIndex: 'name'
      }, {
        title: '餐品价格',
        dataIndex: 'price',
        ellipsis: true,
        customRender: (text, row, index) => {
          if (text !== null) {
            return text + '元 / 100克'
          } else {
            return '- -'
          }
        }
      }, {
        title: '原料',
        dataIndex: 'model',
        ellipsis: true,
        customRender: (text, row, index) => {
          if (row.materials && row.materials.length > 0) {
            return row.materials.map(m => m.materialName).join('、')
          } else {
            return '- -'
          }
        }
      }, {
        title: '餐品类型',
        dataIndex: 'typeName',
        ellipsis: true,
        customRender: (text, row, index) => {
          if (text !== null) {
            return text
          } else {
            return '- -'
          }
        }
      }, {
        title: '创建时间',
        dataIndex: 'createDate',
        ellipsis: true,
        customRender: (text, row, index) => {
          if (text !== null) {
            return text
          } else {
            return '- -'
          }
        }
      }, {
        title: '餐品图片',
        dataIndex: 'images',
        customRender: (text, record, index) => {
          if (!record.images) return <a-avatar shape="square" icon="user" />
          return <a-popover>
            <template slot="content">
              <a-avatar shape="square" size={132} icon="user" src={ 'http://127.0.0.1:9527/imagesWeb/' + record.images.split(',')[0] } />
            </template>
            <a-avatar shape="square" icon="user" src={ 'http://127.0.0.1:9527/imagesWeb/' + record.images.split(',')[0] } />
          </a-popover>
        }
      }]
    },
    materialColumns () {
      return [{
        title: '原材料',
        dataIndex: 'materialId',
        width: 250,
        scopedSlots: { customRender: 'materialIdSelect' }
      }, {
        title: '数量',
        dataIndex: 'quantity',
        width: 150,
        scopedSlots: { customRender: 'quantityInput' }
      }, {
        title: '单位',
        dataIndex: 'unit',
        width: 120,
        scopedSlots: { customRender: 'unitInput' }
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
  mounted () {
    this.fetch()
    this.loadMaterialList()
  },
  methods: {
    loadMaterialList () {
      this.$get('/cos/material-info/list').then((r) => {
        this.materialList = r.data.data || []
      })
    },
    openMaterialRelation (record) {
      this.materialRelationModal.commodityId = record.id
      this.materialRelationModal.visible = true
      this.materialRelationModal.relationList = []

      // 如果已有关系数据，加载它
      if (record.relationListStr) {
        try {
          this.materialRelationModal.relationList = JSON.parse(record.relationListStr)
        } catch (e) {
          console.error('解析原材料关系数据失败', e)
          this.materialRelationModal.relationList = []
        }
      }

      // 如果没有数据，默认添加一行
      if (this.materialRelationModal.relationList.length === 0) {
        this.addMaterialRow()
      }
    },
    addMaterialRow () {
      this.materialRelationModal.relationList.push({
        commodityId: this.materialRelationModal.commodityId,
        materialId: undefined,
        quantity: 0,
        unit: '',
        remark: ''
      })
    },
    removeMaterialRow (index) {
      this.materialRelationModal.relationList.splice(index, 1)
    },
    handleMaterialChange (materialId, index) {
      const material = this.materialList.find(m => m.id === materialId)
      if (material) {
        this.materialRelationModal.relationList[index].unit = material.unit
      }
    },
    filterMaterialOption (input, option) {
      return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    },
    handleMaterialRelationClose () {
      this.materialRelationModal.visible = false
      this.materialRelationModal.relationList = []
      this.materialRelationModal.commodityId = null
    },
    handleMaterialRelationSubmit () {
      // 验证数据
      if (this.materialRelationModal.relationList.length === 0) {
        this.$message.warning('请至少添加一条原材料关联')
        return
      }

      const invalidItem = this.materialRelationModal.relationList.find(item => !item.materialId)
      if (invalidItem) {
        this.$message.warning('请选择所有原材料')
        return
      }

      this.materialRelationModal.loading = true

      // 将关系列表转换为JSON字符串
      const relationListStr = JSON.stringify(this.materialRelationModal.relationList)

      this.$put('/cos/commodity-info', {
        id: this.materialRelationModal.commodityId,
        relationListStr: relationListStr
      }).then((r) => {
        this.$message.success('保存原材料关联成功')
        this.handleMaterialRelationClose()
        this.search()
      }).catch(() => {
        this.materialRelationModal.loading = false
      })
    },
    view (row) {
      this.commodityView.data = row
      this.commodityView.visiable = true
    },
    handlecommodityViewClose () {
      this.commodityView.visiable = false
    },
    onSelectChange (selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    add () {
      this.commodityAdd.visiable = true
    },
    handlecommodityAddClose () {
      this.commodityAdd.visiable = false
    },
    handlecommodityAddSuccess () {
      this.commodityAdd.visiable = false
      this.$message.success('新增餐品成功')
      this.search()
    },
    edit (record) {
      this.$refs.commodityEdit.setFormValues(record)
      this.commodityEdit.visiable = true
    },
    handlecommodityEditClose () {
      this.commodityEdit.visiable = false
    },
    handlecommodityEditSuccess () {
      this.commodityEdit.visiable = false
      this.$message.success('修改餐品成功')
      this.search()
    },
    handleDeptChange (value) {
      this.queryParams.deptId = value || ''
    },
    batchDelete () {
      if (!this.selectedRowKeys.length) {
        this.$message.warning('请选择需要删除的记录')
        return
      }
      let that = this
      this.$confirm({
        title: '确定删除所选中的记录?',
        content: '当您点击确定按钮后，这些记录将会被彻底删除',
        centered: true,
        onOk () {
          let ids = that.selectedRowKeys.join(',')
          that.$delete('/cos/commodity-info/' + ids).then(() => {
            that.$message.success('删除成功')
            that.selectedRowKeys = []
            that.search()
          })
        },
        onCancel () {
          that.selectedRowKeys = []
        }
      })
    },
    search () {
      let {sortedInfo, filteredInfo} = this
      let sortField, sortOrder
      // 获取当前列的排序和列的过滤规则
      if (sortedInfo) {
        sortField = sortedInfo.field
        sortOrder = sortedInfo.order
      }
      this.fetch({
        sortField: sortField,
        sortOrder: sortOrder,
        ...this.queryParams,
        ...filteredInfo
      })
    },
    reset () {
      // 取消选中
      this.selectedRowKeys = []
      // 重置分页
      this.$refs.TableInfo.pagination.current = this.pagination.defaultCurrent
      if (this.paginationInfo) {
        this.paginationInfo.current = this.pagination.defaultCurrent
        this.paginationInfo.pageSize = this.pagination.defaultPageSize
      }
      // 重置列过滤器规则
      this.filteredInfo = null
      // 重置列排序规则
      this.sortedInfo = null
      // 重置查询参数
      this.queryParams = {}
      this.fetch()
    },
    handleTableChange (pagination, filters, sorter) {
      // 将这三个参数赋值给Vue data，用于后续使用
      this.paginationInfo = pagination
      this.filteredInfo = filters
      this.sortedInfo = sorter

      this.fetch({
        sortField: sorter.field,
        sortOrder: sorter.order,
        ...this.queryParams,
        ...filters
      })
    },
    fetch (params = {}) {
      // 显示loading
      this.loading = true
      if (this.paginationInfo) {
        // 如果分页信息不为空，则设置表格当前第几页，每页条数，并设置查询分页参数
        this.$refs.TableInfo.pagination.current = this.paginationInfo.current
        this.$refs.TableInfo.pagination.pageSize = this.paginationInfo.pageSize
        params.size = this.paginationInfo.pageSize
        params.current = this.paginationInfo.current
      } else {
        // 如果分页信息为空，则设置为默认值
        params.size = this.pagination.defaultPageSize
        params.current = this.pagination.defaultCurrent
      }
      this.$get('/cos/commodity-info/page', {
        ...params
      }).then((r) => {
        let data = r.data.data
        const pagination = {...this.pagination}
        pagination.total = data.total
        this.dataSource = data.records
        this.pagination = pagination
        // 数据加载完毕，关闭loading
        this.loading = false
      })
    }
  },
  watch: {}
}
</script>
<style lang="less" scoped>
@import "../../../../static/less/Common";
</style>
