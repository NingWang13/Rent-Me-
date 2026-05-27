class Store {
  constructor(options = {}) {
    this.state = options.state || {};
    this.mutations = options.mutations || {};
    this.actions = options.actions || {};
    this.getters = options.getters || {};
    this.listeners = [];
    this._pendingStateChanges = [];
  }

  getState() {
    return JSON.parse(JSON.stringify(this.state));
  }

  get(key) {
    if (this.getters[key]) {
      return this.getters[key](this.state);
    }
    return this.state[key];
  }

  commit(mutationName, payload) {
    const mutation = this.mutations[mutationName];
    if (!mutation) {
      console.error(`Mutation "${mutationName}" not found`);
      return;
    }

    const prevState = JSON.parse(JSON.stringify(this.state));
    mutation(this.state, payload);
    const nextState = JSON.parse(JSON.stringify(this.state));

    this._notifyListeners({
      type: mutationName,
      payload,
      prevState,
      nextState
    });

    this._persistState();
  }

  async dispatch(actionName, payload) {
    const action = this.actions[actionName];
    if (!action) {
      console.error(`Action "${actionName}" not found`);
      return;
    }

    const storeContext = {
      state: this.state,
      commit: (mutation, data) => this.commit(mutation, data),
      dispatch: (action, data) => this.dispatch(action, data),
      get: (key) => this.get(key),
      getters: this.getters
    };

    try {
      return await action(storeContext, payload);
    } catch (error) {
      console.error(`Action "${actionName}" failed:`, error);
      throw error;
    }
  }

  subscribe(listener) {
    this.listeners.push(listener);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  _notifyListeners(change) {
    this.listeners.forEach(listener => {
      try {
        listener(change);
      } catch (error) {
        console.error('Store listener error:', error);
      }
    });
  }

  _persistState() {
    try {
      const persistKeys = this._getPersistKeys();
      const persistData = {};
      persistKeys.forEach(key => {
        if (this.state[key] !== undefined) {
          persistData[key] = this.state[key];
        }
      });
      if (Object.keys(persistData).length > 0) {
        wx.setStorageSync('store_persist', persistData);
      }
    } catch (error) {
      console.error('Persist state error:', error);
    }
  }

  _getPersistKeys() {
    return ['userInfo', 'token', 'settings', 'elderMode'];
  }

  restorePersistedState() {
    try {
      const persisted = wx.getStorageSync('store_persist');
      if (persisted) {
        Object.keys(persisted).forEach(key => {
          if (this.state[key] !== undefined) {
            this.state[key] = persisted[key];
          }
        });
      }
    } catch (error) {
      console.error('Restore persisted state error:', error);
    }
  }

  reset() {
    const initialState = {};
    Object.keys(this.state).forEach(key => {
      initialState[key] = null;
    });
    this.state = initialState;
    this._notifyListeners({
      type: 'RESET',
      prevState: this.getState(),
      nextState: this.getState()
    });
  }
}

const createStore = (options) => {
  return new Store(options);
};

module.exports = {
  Store,
  createStore
};
