Component({
  properties: {
    iconImage: {
      type: String,
      value: ''
    },
    iconText: {
      type: String,
      value: '📭'
    },
    title: {
      type: String,
      value: '暂无数据'
    },
    desc: {
      type: String,
      value: '这里空空如也'
    },
    actions: {
      type: Array,
      value: []
    },
    customStyle: {
      type: String,
      value: ''
    }
  },

  methods: {
    onActionTap(e) {
      const action = e.currentTarget.dataset.action;
      if (action && action.url) {
        if (action.openType === 'navigateTo') {
          wx.navigateTo({ url: action.url });
        } else if (action.openType === 'switchTab') {
          wx.switchTab({ url: action.url });
        } else if (action.openType === 'redirectTo') {
          wx.redirectTo({ url: action.url });
        } else if (action.openType === 'reLaunch') {
          wx.reLaunch({ url: action.url });
        }
      }
      
      if (action && action.event) {
        this.triggerEvent('action', action);
      }
    }
  }
});
