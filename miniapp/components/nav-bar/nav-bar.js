Component({
  properties: {
    title: {
      type: String,
      value: ''
    },
    showBack: {
      type: Boolean,
      value: false
    },
    bgColor: {
      type: String,
      value: '#ffffff'
    }
  },

  data: {
    statusBarHeight: 0,
    navBarHeight: 44
  },

  lifetimes: {
    attached() {
      const systemInfo = wx.getSystemInfoSync();
      const statusBarHeight = systemInfo.statusBarHeight;
      const navBarHeight = 44;

      this.setData({
        statusBarHeight: statusBarHeight,
        navBarHeight: navBarHeight
      });
    }
  },

  methods: {
    onBack() {
      wx.navigateBack({
        delta: 1
      });
    }
  }
});
