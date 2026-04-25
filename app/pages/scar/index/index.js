
const app = getApp();
let http = require('../../../utils/request')
Page({
    data: {
        StatusBar: app.globalData.StatusBar,
        CustomBar: app.globalData.CustomBar,
        goodsInfo: null,
        groupedGoods: [],
        cartList: [],
        cartCount: 0,
        totalPrice: 0,
        userInfo: null,
        currentCategoryIndex: 0,
        scrollToView: 'category-0',
        categoryPositions: [],
        showDetailPopup: false,
        selectedGoods: null,
        selectedSpec: 'large',
        popupQuantity: 1,
        smallPrice: 0,
        largePrice: 0,
        showCartPopup: false
    },
    onLoad: function (options) {

    },
    onShow() {
        this.isLogin()
    },
    isLogin() {
        wx.getStorage({
            key: 'userInfo',
            success: (res) => {
                this.setData({
                    userInfo: res.data
                })
                this.loadTodayGoods()
                this.loadCartData()
            },
            fail: res => {
                wx.showToast({
                    title: '请先进行登录',
                    icon: 'none',
                    duration: 2000
                })
            }
        })
    },
    loadTodayGoods() {
        http.get('queryCommodityByDate').then((r) => {
            console.log('今日菜品数据:', r)
            if (r && r.commodityList) {
                const grouped = this.groupGoodsByType(r.commodityList)
                this.setData({
                    goodsInfo: r,
                    groupedGoods: grouped
                })
                setTimeout(() => {
                    this.calculateCategoryPositions()
                }, 500)
            }
        })
    },
    groupGoodsByType(goodsList) {
        const groupMap = {}

        goodsList.forEach(item => {
            const typeName = item.typeName || '其他'
            if (!groupMap[typeName]) {
                groupMap[typeName] = {
                    typeName: typeName,
                    goods: []
                }
            }
            groupMap[typeName].goods.push(item)
        })

        return Object.values(groupMap)
    },
    calculateCategoryPositions() {
        const query = wx.createSelectorQuery()
        const positions = []

        this.data.groupedGoods.forEach((item, index) => {
            query.select(`#category-${index}`).boundingClientRect()
        })

        query.exec((res) => {
            res.forEach((rect, index) => {
                if (rect) {
                    positions[index] = rect.top
                }
            })
            this.setData({ categoryPositions: positions })
        })
    },
    loadCartData() {
        const cart = wx.getStorageSync('cart') || []
        this.updateCartSummary(cart)
    },
    updateCartSummary(cart) {
        let count = 0
        let total = 0

        cart.forEach(item => {
            count += item.quantity
            total += item.unitPrice * item.quantity
        })

        this.setData({
            cartList: cart,
            cartCount: count,
            totalPrice: total.toFixed(2)
        })
    },
    getGoodsQuantity(commodityId) {
        const cartItems = this.data.cartList.filter(item => item.commodityId === commodityId)
        let total = 0
        cartItems.forEach(item => {
            total += item.quantity
        })
        return total
    },
    getCategoryCartCount(typeName) {
        let count = 0
        this.data.cartList.forEach(item => {
            if (item.typeName === typeName) {
                count += item.quantity
            }
        })
        return count
    },
    switchCategory(e) {
        const index = e.currentTarget.dataset.index
        this.setData({
            currentCategoryIndex: index,
            scrollToView: `category-${index}`
        })
    },
    onGoodsScroll(e) {
        const scrollTop = e.detail.scrollTop
        const positions = this.data.categoryPositions

        for (let i = positions.length - 1; i >= 0; i--) {
            if (scrollTop >= positions[i] - 100) {
                if (this.data.currentCategoryIndex !== i) {
                    this.setData({
                        currentCategoryIndex: i
                    })
                }
                break
            }
        }
    },
    showGoodsDetail(e) {
        const item = e.currentTarget.dataset.item
        const unitPrice = item.unitPrice
        const smallPrice = parseFloat((unitPrice * 0.6).toFixed(2))

        this.setData({
            showDetailPopup: true,
            selectedGoods: item,
            selectedSpec: 'large',
            popupQuantity: 1,
            smallPrice: smallPrice,
            largePrice: unitPrice
        })
        wx.vibrateShort()
    },
    hideGoodsDetail() {
        this.setData({
            showDetailPopup: false
        })
    },
    stopPropagation() {
    },
    selectSpec(e) {
        const spec = e.currentTarget.dataset.spec
        this.setData({
            selectedSpec: spec
        })
        wx.vibrateShort()
    },
    increasePopupQuantity() {
        this.setData({
            popupQuantity: this.data.popupQuantity + 1
        })
        wx.vibrateShort()
    },
    decreasePopupQuantity() {
        if (this.data.popupQuantity > 1) {
            this.setData({
                popupQuantity: this.data.popupQuantity - 1
            })
            wx.vibrateShort()
        }
    },
    getPopupTotalPrice() {
        if (!this.data.selectedGoods) return '0.00'

        const specPrice = this.data.selectedSpec === 'small' ? this.data.smallPrice : this.data.largePrice

        return (specPrice * this.data.popupQuantity).toFixed(2)
    },
    addToCart(e) {
        const item = e.currentTarget.dataset.item
        const commodity = item.commodity

        let cart = wx.getStorageSync('cart') || []

        const existingIndex = cart.findIndex(c => c.commodityId === commodity.id && c.spec === 'large')

        if (existingIndex > -1) {
            cart[existingIndex].quantity += 1
        } else {
            cart.push({
                commodityId: commodity.id,
                code: commodity.code,
                name: commodity.name,
                price: commodity.price,
                unitPrice: item.unitPrice,
                image: commodity.images,
                model: commodity.model,
                quantity: 1,
                typeName: item.typeName,
                spec: 'large',
                specName: '大份',
                weight: '100g'
            })
        }

        wx.setStorageSync('cart', cart)
        this.updateCartSummary(cart)

        wx.vibrateShort()
        wx.showToast({
            title: '已加入购物车',
            icon: 'success',
            duration: 1500
        })
    },
    decreaseQuantity(e) {
        const commodityId = e.currentTarget.dataset.id
        let cart = wx.getStorageSync('cart') || []

        const index = cart.findIndex(item => item.commodityId === commodityId && item.spec === 'large')

        if (index > -1) {
            if (cart[index].quantity > 1) {
                cart[index].quantity -= 1
            } else {
                cart.splice(index, 1)
            }

            wx.setStorageSync('cart', cart)
            this.updateCartSummary(cart)
            wx.vibrateShort()
        }
    },
    confirmAddToCart() {
        const item = this.data.selectedGoods
        const commodity = item.commodity
        const spec = this.data.selectedSpec
        const quantity = this.data.popupQuantity

        const specPrice = spec === 'small' ? this.data.smallPrice : this.data.largePrice
        const specName = spec === 'small' ? '小份' : '大份'
        const weight = spec === 'small' ? '50g' : '100g'

        let cart = wx.getStorageSync('cart') || []

        const existingIndex = cart.findIndex(c => c.commodityId === commodity.id && c.spec === spec)

        if (existingIndex > -1) {
            cart[existingIndex].quantity += quantity
        } else {
            cart.push({
                commodityId: commodity.id,
                code: commodity.code,
                name: commodity.name,
                price: commodity.price,
                unitPrice: specPrice,
                image: commodity.images,
                model: commodity.model,
                quantity: quantity,
                typeName: item.typeName,
                spec: spec,
                specName: specName,
                weight: weight
            })
        }

        wx.setStorageSync('cart', cart)
        this.updateCartSummary(cart)

        this.hideGoodsDetail()

        wx.showToast({
            title: `已加入${quantity}份`,
            icon: 'success',
            duration: 1500
        })
    },
    showCartDetail() {
        this.setData({
            showCartPopup: true
        })
    },
    hideCartDetail() {
        this.setData({
            showCartPopup: false
        })
    },
    increaseCartItem(e) {
        const index = e.currentTarget.dataset.index
        let cart = this.data.cartList

        cart[index].quantity += 1

        wx.setStorageSync('cart', cart)
        this.updateCartSummary(cart)
        wx.vibrateShort()
    },
    decreaseCartItem(e) {
        const index = e.currentTarget.dataset.index
        let cart = this.data.cartList

        if (cart[index].quantity > 1) {
            cart[index].quantity -= 1
            wx.setStorageSync('cart', cart)
            this.updateCartSummary(cart)
            wx.vibrateShort()
        }
    },
    deleteCartItem(e) {
        const index = e.currentTarget.dataset.index
        let cart = this.data.cartList

        wx.showModal({
            title: '提示',
            content: '确定要删除这个商品吗？',
            success: (res) => {
                if (res.confirm) {
                    cart.splice(index, 1)
                    wx.setStorageSync('cart', cart)
                    this.updateCartSummary(cart)
                    wx.showToast({
                        title: '已删除',
                        icon: 'success',
                        duration: 1500
                    })
                }
            }
        })
    },
    clearCart() {
        wx.showModal({
            title: '提示',
            content: '确定要清空购物车吗？',
            success: (res) => {
                if (res.confirm) {
                    wx.setStorageSync('cart', [])
                    this.updateCartSummary([])
                    wx.showToast({
                        title: '已清空',
                        icon: 'success',
                        duration: 1500
                    })
                }
            }
        })
    },
    goToCart() {
        wx.navigateTo({
            url: '/pages/scar/order/index'
        })
    },
    goToSettlement() {
        const cart = wx.getStorageSync('cart') || []

        if (cart.length === 0) {
            wx.showToast({
                title: '购物车是空的',
                icon: 'none',
                duration: 2000
            })
            return
        }

        wx.navigateTo({
            url: '/pages/scar/order/index?type=checkout'
        })
    }
});
