const { createStore } = require('./store');

const store = createStore({
  state: {
    userInfo: null,
    token: null,
    isLogin: false,
    elderMode: false,
    settings: {
      notificationEnabled: true,
      darkMode: false,
      fontSize: 'normal'
    },
    cart: [],
    wishlist: [],
    unreadCount: 0
  },

  mutations: {
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo;
      state.isLogin = !!userInfo;
    },

    SET_TOKEN(state, token) {
      state.token = token;
      state.isLogin = !!token;
    },

    CLEAR_AUTH(state) {
      state.userInfo = null;
      state.token = null;
      state.isLogin = false;
    },

    SET_ELDER_MODE(state, enabled) {
      state.elderMode = enabled;
    },

    SET_SETTING(state, { key, value }) {
      state.settings[key] = value;
    },

    SET_UNREAD_COUNT(state, count) {
      state.unreadCount = count;
    },

    INCREMENT_UNREAD_COUNT(state) {
      state.unreadCount += 1;
    },

    ADD_TO_CART(state, item) {
      const exists = state.cart.find(i => i.id === item.id);
      if (exists) {
        exists.quantity += item.quantity || 1;
      } else {
        state.cart.push({ ...item, quantity: item.quantity || 1 });
      }
    },

    REMOVE_FROM_CART(state, itemId) {
      state.cart = state.cart.filter(i => i.id !== itemId);
    },

    CLEAR_CART(state) {
      state.cart = [];
    },

    ADD_TO_WISHLIST(state, item) {
      const exists = state.wishlist.find(i => i.id === item.id);
      if (!exists) {
        state.wishlist.push(item);
      }
    },

    REMOVE_FROM_WISHLIST(state, itemId) {
      state.wishlist = state.wishlist.filter(i => i.id !== itemId);
    }
  },

  actions: {
    async login({ commit }, { token, userInfo }) {
      commit('SET_TOKEN', token);
      commit('SET_USER_INFO', userInfo);
    },

    async logout({ commit }) {
      commit('CLEAR_AUTH');
      wx.removeStorageSync('token');
      wx.removeStorageSync('userInfo');
    },

    async fetchUserInfo({ commit, state }) {
      if (!state.token) return;
      try {
        const { get } = require('./request');
        const res = await get('/api/v1/user/info');
        if (res.code === 200) {
          commit('SET_USER_INFO', res.data);
        }
      } catch (error) {
        console.error('Fetch user info failed:', error);
      }
    },

    async fetchUnreadCount({ commit }) {
      try {
        const { get } = require('./request');
        const res = await get('/api/v1/message/unread-count');
        if (res.code === 200) {
          commit('SET_UNREAD_COUNT', res.data.count || 0);
        }
      } catch (error) {
        console.error('Fetch unread count failed:', error);
      }
    },

    toggleElderMode({ commit, state }) {
      commit('SET_ELDER_MODE', !state.elderMode);
      wx.setStorageSync('elderMode', !state.elderMode);
    }
  },

  getters: {
    isLoggedIn: (state) => !!state.token,
    userNickname: (state) => state.userInfo?.nickname || '未登录',
    userAvatar: (state) => state.userInfo?.avatar || '/images/default-avatar.png',
    cartCount: (state) => state.cart.reduce((sum, item) => sum + item.quantity, 0),
    wishlistCount: (state) => state.wishlist.length,
    hasUnreadMessages: (state) => state.unreadCount > 0
  }
});

store.restorePersistedState();

module.exports = store;
