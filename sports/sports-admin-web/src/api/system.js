import http from '../utils/http'

// 管理员列表
export const getSysUserList = (params) => http.get('/sys/user/list', { params })

// 管理员详情
export const getSysUserDetail = (id) => http.get(`/sys/user/detail/${id}`)

// 新增管理员
export const addSysUser = (data) => http.post('/sys/user', data)

// 修改管理员
export const updateSysUser = (data) => http.put('/sys/user', data)

// 删除管理员
export const deleteSysUser = (id) => http.delete(`/sys/user/${id}`)

// 重置密码
export const resetPassword = (data) => http.put('/sys/user/password', data)

// 更新管理员状态
export const updateSysUserStatus = (data) => http.put('/sys/user/status', data)

// 角色列表
export const getSysRoleList = (params) => http.get('/sys/role/list', { params })

// 所有角色
export const getAllRoles = () => http.get('/sys/role/all')

// 新增角色
export const addSysRole = (data) => http.post('/sys/role', data)

// 修改角色
export const updateSysRole = (data) => http.put('/sys/role', data)

// 删除角色
export const deleteSysRole = (id) => http.delete(`/sys/role/${id}`)

// 角色的菜单ID
export const getRoleMenuIds = (roleId) => http.get(`/sys/role/menus/${roleId}`)

// 分配角色菜单
export const assignRoleMenus = (roleId, menuIds) => http.post(`/sys/role/menus/${roleId}`, menuIds)

// 菜单列表
export const getSysMenuList = () => http.get('/sys/menu/list')

// 菜单树
export const getSysMenuTree = () => http.get('/sys/menu/tree')

// 当前用户菜单（根据角色权限）
export const getUserMenus = () => http.get('/sys/menu/user')

// 新增菜单
export const addSysMenu = (data) => http.post('/sys/menu', data)

// 修改菜单
export const updateSysMenu = (data) => http.put('/sys/menu', data)

// 删除菜单
export const deleteSysMenu = (id) => http.delete(`/sys/menu/${id}`)

// ==== 小程序用户管理 ====
// 用户列表
export const getMemberList = (params) => http.get('/member/list', { params })

// 修改用户状态
export const updateMemberStatus = (data) => http.put('/member/status', data)
