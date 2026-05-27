const app = getApp();
const { post } = require('../../utils/request');

Page({
  data: {
    username: '',
    password: ''
  },

  onUsernameInput(e) {
    this.setData({
      username: e.detail.value
    });
  },

  onPasswordInput(e) {
    this.setData({
      password: e.detail.value
    });
  },

  async handleLogin() {
    const { username, password } = this.data;

    if (!username.trim()) {
      wx.showToast({ title: '请输入用户名', icon: 'none' });
      return;
    }

    if (!password.trim()) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '登录中...', mask: true });

    try {
      const result = await post('/user/login', {
        username: username.trim(),
        password: password.trim()
      }, { showLoading: false });

      wx.hideLoading();

      if (result.code === 200) {
        const token = result.data;
        
        // 获取用户信息
        const userResult = await get('/user/info', {}, { 
          header: { 'Authorization': `Bearer ${token}` },
          showLoading: false 
        });

        if (userResult.code === 200) {
          app.login(token, userResult.data);
          
          wx.showToast({
            title: '登录成功',
            icon: 'success',
            duration: 1500
          });

          setTimeout(() => {
            wx.switchTab({ url: '/pages/index/index' });
          }, 1500);
        }
      } else {
        wx.showToast({
          title: result.message || '登录失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.hideLoading();
      console.error('登录失败:', error);
      wx.showToast({
        title: error.message || '登录失败，请重试',
        icon: 'none'
      });
    }
  },

  goToRegister() {
    wx.navigateTo({
      url: '/pages/auth/register/register'
    });
  },

  onLoad() {
    // 检查是否已登录
    const token = wx.getStorageSync('token');
    if (token) {
      wx.switchTab({ url: '/pages/index/index' });
    }
  }
});
