<template>
  <div class="user-center">
    <h2>用户中心</h2>
    <p class="hint">用于管理系统用户、角色权限等信息。</p>

    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 人员管理 -->
    <div v-show="activeTab === 'staff'" class="tab-content">
      <section class="section">
        <header class="section-header">
          <div>
            <h3>人员管理</h3>
            <p class="sub-hint">管理系统员工信息</p>
          </div>
          <button class="add-btn" @click="showStaffDialog = true">添加人员</button>
        </header>

        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>姓名</th>
                <th>工号</th>
                <th>部门</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="staff in staffList" :key="staff.staffId">
                <td>{{ staff.staffId }}</td>
                <td>{{ staff.staffname || '-' }}</td>
                <td>{{ staff.staffcode || '-' }}</td>
                <td>{{ staff.department || '-' }}</td>
                <td>
                  <button class="edit-btn" @click="editStaff(staff)">编辑</button>
                  <button class="delete-btn" @click="deleteStaff(staff.staffId)">删除</button>
                </td>
              </tr>
              <tr v-if="!staffList.length">
                <td colspan="5" class="empty-cell">暂无人员数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 添加/编辑人员对话框 -->
      <div v-if="showStaffDialog" class="dialog-overlay" @click.self="showStaffDialog = false">
        <div class="dialog">
          <h3>{{ editingStaffId ? '编辑人员' : '添加人员' }}</h3>
          <form @submit.prevent="editingStaffId ? updateStaff() : addStaff()">
            <label>
              姓名
              <input v-model.trim="staffForm.staffname" type="text" required />
            </label>
            <label v-if="!editingStaffId">
              工号
              <input v-model.trim="staffForm.staffcode" type="text" required />
            </label>
            <label v-else>
              工号
              <input v-model.trim="staffForm.staffcode" type="text" disabled />
            </label>
            <label>
              密码
              <input v-model.trim="staffForm.password" type="password" :required="!editingStaffId" :placeholder="editingStaffId ? '留空则不修改密码' : ''" />
            </label>
            <label>
              部门
              <input v-model.trim="staffForm.department" type="text" />
            </label>
            <label>
              角色
              <select v-model.number="staffForm.roleId" required>
                <option :value="null">请选择角色</option>
                <option v-for="role in roleList" :key="role.roleId" :value="role.roleId">
                  {{ role.roleName }}
                </option>
              </select>
            </label>
            <div class="dialog-actions">
              <button type="button" class="cancel-btn" @click="closeStaffDialog">取消</button>
              <button type="submit" class="submit-btn" :disabled="staffLoading">
                {{ staffLoading ? (editingStaffId ? '更新中...' : '添加中...') : '确定' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 设备管理 -->
    <div v-show="activeTab === 'device'" class="tab-content">
      <section class="section">
        <header class="section-header">
          <div>
            <h3>设备管理</h3>
            <p class="sub-hint">管理系统设备信息</p>
          </div>
          <button class="add-btn" @click="showDeviceDialog = true">添加设备</button>
        </header>

        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>设备名称</th>
                <th>设备编号</th>
                <th>位置</th>
                <th>设备类型</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="device in deviceList" :key="device.deviceId">
                <td>{{ device.deviceId }}</td>
                <td>{{ device.deviceName || '-' }}</td>
                <td>{{ device.deviceCode || '-' }}</td>
                <td>{{ device.location || '-' }}</td>
                <td>{{ device.deviceType || '-' }}</td>
                <td>
                  <button class="edit-btn" @click="editDevice(device)">编辑</button>
                  <button class="delete-btn" @click="deleteDevice(device.deviceCode)">删除</button>
                </td>
              </tr>
              <tr v-if="!deviceList.length">
                <td colspan="6" class="empty-cell">暂无设备数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 添加/编辑设备对话框 -->
      <div v-if="showDeviceDialog" class="dialog-overlay" @click.self="showDeviceDialog = false">
        <div class="dialog">
          <h3>{{ editingDeviceCode ? '编辑设备' : '添加设备' }}</h3>
          <form @submit.prevent="editingDeviceCode ? updateDevice() : addDevice()">
            <label>
              设备名称
              <input v-model.trim="deviceForm.deviceName" type="text" required />
            </label>
            <label v-if="!editingDeviceCode">
              设备编号
              <input v-model.trim="deviceForm.deviceCode" type="text" required />
            </label>
            <label v-else>
              设备编号
              <input v-model.trim="deviceForm.deviceCode" type="text" disabled />
            </label>
            <label>
              位置
              <input v-model.trim="deviceForm.location" type="text" />
            </label>
            <label>
              设备类型
              <select v-model="deviceForm.deviceType" required>
                <option value="">请选择类型</option>
                <option value="D4">D4</option>
                <option value="DB">DB</option>
              </select>
            </label>
            <div class="dialog-actions">
              <button type="button" class="cancel-btn" @click="closeDeviceDialog">取消</button>
              <button type="submit" class="submit-btn" :disabled="deviceLoading">
                {{ deviceLoading ? (editingDeviceCode ? '更新中...' : '添加中...') : '确定' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 权限管理 -->
    <div v-show="activeTab === 'permission'" class="tab-content">
      <section class="section">
        <header class="section-header">
          <div>
            <h3>权限管理</h3>
            <p class="sub-hint">管理角色对应的权限</p>
          </div>
        </header>

        <div class="permission-management">
          <!-- 左侧：角色列表 -->
          <div class="role-list-container">
            <h4>角色列表</h4>
            <div class="role-list">
              <div
                v-for="role in roleList"
                :key="role.roleId"
                :class="['role-item', { active: selectedRoleId === role.roleId }]"
                @click="selectRole(role.roleId)"
              >
                <span class="role-name">{{ role.roleName }}</span>
                <span class="role-desc" v-if="role.roleDescription">{{ role.roleDescription }}</span>
              </div>
              <div v-if="!roleList.length" class="empty-role">暂无角色数据</div>
            </div>
          </div>

          <!-- 右侧：权限列表 -->
          <div class="permission-list-container">
            <div v-if="!selectedRoleId" class="no-role-selected">
              <p>请先选择一个角色</p>
            </div>
            <div v-else>
              <div class="permission-header">
                <h4>{{ selectedRoleName }} 的权限</h4>
                <button class="add-btn" @click="showAddPermissionDialog = true">添加权限</button>
              </div>
              <div class="table-container">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>权限名称</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="rp in rolePermissionList" :key="rp.id">
                      <td>{{ rp.permission?.permissionId }}</td>
                      <td>{{ rp.permission?.permission || '-' }}</td>
                      <td>
                        <button class="delete-btn" @click="removePermissionFromRole(rp.permission.permissionId)">移除</button>
                      </td>
                    </tr>
                    <tr v-if="!rolePermissionList.length">
                      <td colspan="3" class="empty-cell">该角色暂无权限</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 添加权限到角色对话框 -->
      <div v-if="showAddPermissionDialog" class="dialog-overlay" @click.self="showAddPermissionDialog = false">
        <div class="dialog">
          <h3>为 {{ selectedRoleName }} 添加权限</h3>
          <form @submit.prevent="addPermissionToRole">
            <label>
              选择权限
              <select v-model.number="selectedPermissionId" required>
                <option :value="null">请选择权限</option>
                <option
                  v-for="perm in availablePermissions"
                  :key="perm.permissionId"
                  :value="perm.permissionId"
                >
                  {{ perm.permission }}
                </option>
              </select>
            </label>
            <div class="dialog-actions">
              <button type="button" class="cancel-btn" @click="showAddPermissionDialog = false">取消</button>
              <button type="submit" class="submit-btn" :disabled="permissionLoading">
                {{ permissionLoading ? '添加中...' : '确定' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch, computed } from 'vue';
import axios from 'axios';

const apiBase = import.meta.env.VITE_API_BASE_URL || '';
const activeTab = ref('staff');

const tabs = [
  { key: 'staff', label: '人员管理' },
  { key: 'device', label: '设备管理' },
  { key: 'permission', label: '权限管理' }
];

// 人员管理
const staffList = ref([]);
const roleList = ref([]);
const showStaffDialog = ref(false);
const staffLoading = ref(false);
const editingStaffId = ref(null);
const staffForm = reactive({
  staffname: '',
  staffcode: '',
  password: '',
  department: '',
  roleId: null
});

// 设备管理
const deviceList = ref([]);
const showDeviceDialog = ref(false);
const deviceLoading = ref(false);
const editingDeviceCode = ref(null);
const deviceForm = reactive({
  deviceName: '',
  deviceCode: '',
  location: '',
  deviceType: ''
});

// 权限管理（角色权限管理）
const selectedRoleId = ref(null);
const selectedRoleName = ref('');
const rolePermissionList = ref([]);
const allPermissionList = ref([]);
const showAddPermissionDialog = ref(false);
const permissionLoading = ref(false);
const selectedPermissionId = ref(null);

// 获取所有角色
const fetchRoles = async () => {
  try {
    const { data } = await axios.get(`${apiBase}/api/roles/list`);
    roleList.value = data?.data ?? [];
  } catch (error) {
    console.error('获取角色列表失败:', error);
  }
};

// 获取所有人员
const fetchStaff = async () => {
  try {
    const { data } = await axios.get(`${apiBase}/api/admin/users/list`);
    staffList.value = data?.data ?? [];
  } catch (error) {
    console.error('获取人员列表失败:', error);
    alert('获取人员列表失败: ' + (error.response?.data?.msg || error.message));
  }
};

// 添加人员
const addStaff = async () => {
  if (staffLoading.value) return;
  staffLoading.value = true;
  try {
    const { data } = await axios.post(`${apiBase}/api/admin/users/add`, {
      staff: {
        staffname: staffForm.staffname,
        staffcode: staffForm.staffcode,
        password: staffForm.password,
        department: staffForm.department || null
      },
      roleId: staffForm.roleId
    });
    if (data?.code === '200') {
      alert('人员添加成功');
      closeStaffDialog();
      await fetchStaff();
    } else {
      alert('添加失败: ' + (data?.msg || '未知错误'));
    }
  } catch (error) {
    console.error('添加人员失败:', error);
    alert('添加人员失败: ' + (error.response?.data?.msg || error.message));
  } finally {
    staffLoading.value = false;
  }
};

// 编辑人员
const editStaff = async (staff) => {
  editingStaffId.value = staff.staffId;
  Object.assign(staffForm, {
    staffname: staff.staffname || '',
    staffcode: staff.staffcode || '',
    password: '',
    department: staff.department || '',
    roleId: null
  });
  // 获取当前人员的角色
  try {
    const { data } = await axios.get(`${apiBase}/api/admin/users/${staff.staffId}/role`);
    if (data?.code === '200' && data?.data) {
      staffForm.roleId = data.data.roleId;
    }
  } catch (error) {
    console.error('获取人员角色失败:', error);
  }
  showStaffDialog.value = true;
};

// 更新人员
const updateStaff = async () => {
  if (staffLoading.value) return;
  staffLoading.value = true;
  try {
    const requestData = {
      staff: {
        staffname: staffForm.staffname,
        password: staffForm.password || null,
        department: staffForm.department || null
      },
      roleId: staffForm.roleId
    };
    // 如果密码为空，则不传递密码字段
    if (!requestData.staff.password) {
      delete requestData.staff.password;
    }
    const { data } = await axios.put(`${apiBase}/api/admin/users/update/${editingStaffId.value}`, requestData);
    if (data?.code === '200') {
      alert('人员更新成功');
      closeStaffDialog();
      await fetchStaff();
    } else {
      alert('更新失败: ' + (data?.msg || '未知错误'));
    }
  } catch (error) {
    console.error('更新人员失败:', error);
    alert('更新人员失败: ' + (error.response?.data?.msg || error.message));
  } finally {
    staffLoading.value = false;
  }
};

// 关闭人员对话框
const closeStaffDialog = () => {
  showStaffDialog.value = false;
  editingStaffId.value = null;
  Object.assign(staffForm, {
    staffname: '',
    staffcode: '',
    password: '',
    department: '',
    roleId: null
  });
};

// 删除人员
const deleteStaff = async (staffId) => {
  if (!confirm('确定要删除该人员吗？')) return;
  try {
    const { data } = await axios.delete(`${apiBase}/api/admin/users/delete/${staffId}`);
    if (data?.code === '200') {
      alert('人员删除成功');
      await fetchStaff();
    } else {
      alert('删除失败: ' + (data?.msg || '未知错误'));
    }
  } catch (error) {
    console.error('删除人员失败:', error);
    alert('删除人员失败: ' + (error.response?.data?.msg || error.message));
  }
};

// 获取所有设备
const fetchDevices = async () => {
  try {
    const { data } = await axios.get(`${apiBase}/api/device/getall`);
    deviceList.value = data?.data ?? [];
  } catch (error) {
    console.error('获取设备列表失败:', error);
    alert('获取设备列表失败: ' + (error.response?.data?.msg || error.message));
  }
};

// 添加设备
const addDevice = async () => {
  if (deviceLoading.value) return;
  deviceLoading.value = true;
  try {
    const { data } = await axios.post(`${apiBase}/api/device/add`, deviceForm);
    if (data?.code === '200') {
      alert('设备添加成功');
      closeDeviceDialog();
      await fetchDevices();
    } else {
      alert('添加失败: ' + (data?.msg || '未知错误'));
    }
  } catch (error) {
    console.error('添加设备失败:', error);
    alert('添加设备失败: ' + (error.response?.data?.msg || error.message));
  } finally {
    deviceLoading.value = false;
  }
};

// 编辑设备
const editDevice = (device) => {
  editingDeviceCode.value = device.deviceCode;
  Object.assign(deviceForm, {
    deviceName: device.deviceName || '',
    deviceCode: device.deviceCode || '',
    location: device.location || '',
    deviceType: device.deviceType || ''
  });
  showDeviceDialog.value = true;
};

// 更新设备
const updateDevice = async () => {
  if (deviceLoading.value) return;
  deviceLoading.value = true;
  try {
    const updateData = {
      deviceName: deviceForm.deviceName,
      location: deviceForm.location,
      deviceType: deviceForm.deviceType
    };
    const { data } = await axios.patch(`${apiBase}/api/device/patch/${editingDeviceCode.value}`, updateData);
    if (data?.code === '200') {
      alert('设备更新成功');
      closeDeviceDialog();
      await fetchDevices();
    } else {
      alert('更新失败: ' + (data?.msg || '未知错误'));
    }
  } catch (error) {
    console.error('更新设备失败:', error);
    alert('更新设备失败: ' + (error.response?.data?.msg || error.message));
  } finally {
    deviceLoading.value = false;
  }
};

// 关闭设备对话框
const closeDeviceDialog = () => {
  showDeviceDialog.value = false;
  editingDeviceCode.value = null;
  Object.assign(deviceForm, {
    deviceName: '',
    deviceCode: '',
    location: '',
    deviceType: ''
  });
};

// 删除设备
const deleteDevice = async (deviceCode) => {
  if (!confirm('确定要删除该设备吗？')) return;
  try {
    const { data } = await axios.delete(`${apiBase}/api/device/delete/${deviceCode}`);
    if (data?.code === '200') {
      alert('设备删除成功');
      await fetchDevices();
    } else {
      alert('删除失败: ' + (data?.msg || '未知错误'));
    }
  } catch (error) {
    console.error('删除设备失败:', error);
    alert('删除设备失败: ' + (error.response?.data?.msg || error.message));
  }
};

// 选择角色
const selectRole = async (roleId) => {
  selectedRoleId.value = roleId;
  const role = roleList.value.find(r => r.roleId === roleId);
  selectedRoleName.value = role ? role.roleName : '';
  await fetchRolePermissions(roleId);
};

// 获取角色的权限
const fetchRolePermissions = async (roleId) => {
  try {
    const { data } = await axios.get(`${apiBase}/api/roles/${roleId}/permissions`);
    if (data?.code === '200') {
      rolePermissionList.value = data?.data ?? [];
    }
  } catch (error) {
    console.error('获取角色权限失败:', error);
    alert('获取角色权限失败: ' + (error.response?.data?.msg || error.message));
  }
};

// 获取所有权限（用于添加权限到角色时选择）
const fetchAllPermissions = async () => {
  try {
    const { data } = await axios.get(`${apiBase}/api/permissions/list`);
    allPermissionList.value = data?.data ?? [];
  } catch (error) {
    console.error('获取权限列表失败:', error);
  }
};

// 计算可用权限（排除已拥有的权限）
const availablePermissions = computed(() => {
  if (!selectedRoleId.value) return allPermissionList.value;
  const ownedPermissionIds = rolePermissionList.value.map(rp => rp.permission?.permissionId);
  return allPermissionList.value.filter(perm => !ownedPermissionIds.includes(perm.permissionId));
});

// 为角色添加权限
const addPermissionToRole = async () => {
  if (permissionLoading.value || !selectedPermissionId.value) return;
  permissionLoading.value = true;
  try {
    const { data } = await axios.post(
      `${apiBase}/api/roles/${selectedRoleId.value}/add-permission/${selectedPermissionId.value}`
    );
    if (data?.code === '200') {
      alert('权限添加成功');
      showAddPermissionDialog.value = false;
      selectedPermissionId.value = null;
      await fetchRolePermissions(selectedRoleId.value);
    } else {
      alert('添加失败: ' + (data?.msg || '未知错误'));
    }
  } catch (error) {
    console.error('添加权限失败:', error);
    alert('添加权限失败: ' + (error.response?.data?.msg || error.message));
  } finally {
    permissionLoading.value = false;
  }
};

// 从角色移除权限
const removePermissionFromRole = async (permissionId) => {
  if (!confirm('确定要从该角色移除该权限吗？')) return;
  try {
    const { data } = await axios.delete(
      `${apiBase}/api/roles/${selectedRoleId.value}/del-permission/${permissionId}`
    );
    if (data?.code === '200') {
      alert('权限移除成功');
      await fetchRolePermissions(selectedRoleId.value);
    } else {
      alert('移除失败: ' + (data?.msg || '未知错误'));
    }
  } catch (error) {
    console.error('移除权限失败:', error);
    alert('移除权限失败: ' + (error.response?.data?.msg || error.message));
  }
};

// 根据当前选中的标签页加载数据
const loadDataForTab = async () => {
  if (activeTab.value === 'staff') {
    await fetchStaff();
    await fetchRoles();
  } else if (activeTab.value === 'device') {
    await fetchDevices();
  } else if (activeTab.value === 'permission') {
    await fetchRoles();
    await fetchAllPermissions();
    // 重置选中状态
    selectedRoleId.value = null;
    selectedRoleName.value = '';
    rolePermissionList.value = [];
  }
};

// 监听标签页切换，自动加载数据
watch(activeTab, () => {
  loadDataForTab();
});

onMounted(() => {
  loadDataForTab();
});
</script>

<style scoped>
.user-center h2 {
  margin: 0 0 0.5rem;
}

.hint {
  color: #9ca3af;
  margin-bottom: 1.5rem;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.tab-btn {
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  color: #94a3b8;
  cursor: pointer;
  font-size: 0.95rem;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: #cbd5f5;
}

.tab-btn.active {
  color: #38bdf8;
  border-bottom-color: #38bdf8;
}

.tab-content {
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.section {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  padding: 1.5rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h3 {
  margin: 0;
}

.sub-hint {
  margin: 0.2rem 0 0;
  color: #94a3b8;
  font-size: 0.85rem;
}

.add-btn {
  padding: 0.5rem 1rem;
  background: #38bdf8;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.2s;
}

.add-btn:hover {
  background: #0ea5e9;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  color: #000000;
}

.data-table thead {
  background: rgba(148, 163, 184, 0.1);
}

.data-table th {
  padding: 0.75rem;
  text-align: left;
  font-weight: 600;
  font-size: 0.9rem;
  color: #000000;
  border-bottom: 2px solid rgba(148, 163, 184, 0.5);
}

.data-table td {
  padding: 0.75rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.3);
  font-size: 0.9rem;
  color: #000000;
}

.data-table tbody tr:hover {
  background: rgba(148, 163, 184, 0.05);
}

.empty-cell {
  text-align: center;
  color: #94a3b8;
  padding: 2rem;
}

.edit-btn {
  padding: 0.4rem 0.8rem;
  background: #38bdf8;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background 0.2s;
  margin-right: 0.5rem;
}

.edit-btn:hover {
  background: #0ea5e9;
}

.delete-btn {
  padding: 0.4rem 0.8rem;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background 0.2s;
}

.delete-btn:hover {
  background: #dc2626;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  padding: 2rem;
  width: 90%;
  max-width: 500px;
  color: #cbd5f5;
}

.dialog h3 {
  margin: 0 0 1.5rem;
  color: #cbd5f5;
}

.dialog label {
  display: block;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  color: #94a3b8;
}

.dialog input,
.dialog select {
  width: 100%;
  padding: 0.6rem;
  margin-top: 0.4rem;
  background: rgba(2, 6, 23, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 8px;
  color: #cbd5f5;
  font-size: 0.9rem;
}

.dialog input:focus,
.dialog select:focus {
  outline: none;
  border-color: #38bdf8;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.cancel-btn,
.submit-btn {
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.cancel-btn {
  background: rgba(148, 163, 184, 0.2);
  color: #cbd5f5;
}

.cancel-btn:hover {
  background: rgba(148, 163, 184, 0.3);
}

.submit-btn {
  background: #38bdf8;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background: #0ea5e9;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 权限管理样式 */
.permission-management {
  display: flex;
  gap: 1.5rem;
  margin-top: 1rem;
}

.role-list-container {
  flex: 0 0 300px;
  background: #ffffff;
  border-radius: 12px;
  padding: 1rem;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.role-list-container h4 {
  margin: 0 0 1rem;
  color: #000000;
  font-size: 1rem;
}

.role-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.role-item {
  padding: 0.75rem;
  background: rgba(148, 163, 184, 0.1);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  color: #000000;
}

.role-item:hover {
  background: rgba(148, 163, 184, 0.15);
  border-color: rgba(56, 189, 248, 0.3);
}

.role-item.active {
  background: rgba(56, 189, 248, 0.2);
  border-color: #38bdf8;
}

.role-name {
  display: block;
  color: #000000;
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.role-desc {
  display: block;
  color: #94a3b8;
  font-size: 0.85rem;
}

.empty-role {
  text-align: center;
  color: #94a3b8;
  padding: 2rem;
}

.permission-list-container {
  flex: 1;
  background: #ffffff;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.permission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.permission-header h4 {
  margin: 0;
  color: #000000;
}

.no-role-selected {
  text-align: center;
  padding: 4rem 2rem;
  color: #94a3b8;
}

.no-role-selected p {
  margin: 0;
  font-size: 1.1rem;
}

/* 主题设置样式 */
.theme-settings {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.theme-section {
  background: rgba(2, 6, 23, 0.5);
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.theme-section h4 {
  margin: 0 0 0.5rem;
  color: #cbd5f5;
  font-size: 1.1rem;
}

.theme-hint {
  margin: 0 0 1.5rem;
  color: #94a3b8;
  font-size: 0.9rem;
}

.color-picker {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
}

.color-option {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1rem;
  background: rgba(148, 163, 184, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.color-option:hover {
  background: rgba(148, 163, 184, 0.15);
  border-color: rgba(56, 189, 248, 0.3);
  transform: translateY(-2px);
}

.color-option.active {
  background: rgba(56, 189, 248, 0.2);
  border-color: #38bdf8;
}

.color-preview {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  margin-bottom: 0.75rem;
  border: 2px solid rgba(148, 163, 184, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.color-name {
  color: #cbd5f5;
  font-size: 0.9rem;
  font-weight: 500;
}

.check-mark {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 24px;
  height: 24px;
  background: #38bdf8;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: bold;
}

.custom-color {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.color-input {
  width: 80px;
  height: 50px;
  border: 2px solid rgba(148, 163, 184, 0.3);
  border-radius: 8px;
  cursor: pointer;
  background: none;
}

.color-input::-webkit-color-swatch-wrapper {
  padding: 0;
}

.color-input::-webkit-color-swatch {
  border: none;
  border-radius: 6px;
}

.color-text {
  flex: 1;
  padding: 0.75rem;
  background: rgba(2, 6, 23, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 8px;
  color: #cbd5f5;
  font-size: 0.9rem;
  font-family: monospace;
}

.color-text:focus {
  outline: none;
  border-color: #38bdf8;
}
</style>
