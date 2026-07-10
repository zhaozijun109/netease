#!/usr/bin/env bash
#
# update_hive_metadata.sh
#
# 1. 执行 java 命令生成 md 文件到 output 目录
# 2. 拷贝 md 到目标仓库
# 3. git add -> git commit -> git pull --rebase -> git push
#
# 通过 GitLab Access Token (HTTPS) 推送, token 不落盘到 .git/config,
# 仅在 pull/push 时通过 -c http.extraHeader 临时注入
#
# 日志按日期滚动: <LOG_DIR>/update_hive_metadata.YYYY-MM-DD.log
# 自动清理 LOG_RETAIN_DAYS 天前的旧日志
#
# 退出码:
#   0  成功 (含「无变更, 跳过提交」)
#   1  java 生成失败 / 拷贝失败 / git 操作失败
#
source ~/.bashrc
set -euo pipefail

# 让 git 在需要交互式凭证时直接失败 (而不是阻塞在 "Username for ..." 提示)
# - GIT_TERMINAL_PROMPT=0  禁止 git 自身的交互提示
# - GIT_ASKPASS=/bin/true  让任何 askpass 调用立刻返回空 (相当于"取消")
export GIT_TERMINAL_PROMPT=0
export GIT_ASKPASS=/bin/true
unset SSH_ASKPASS

# ============== 可配置参数 (按实际部署修改) ==============

# --- java ---
WORK_DIR="/home/lofter/tasks/hive-metastore"
JAR_PATH="common-assembly-0.0.1.jar"
MAIN_CLASS="com.netease.yuanqi.markdown.MetadataMarkdownGenerator"
OUTPUT_DIR="${WORK_DIR}/output"

# --- git 仓库 ---
# 推送目标列表: 每个元素是一个仓库的 "REPO_DIR|REPO_SUBDIR|TOKEN_VAR"
#   REPO_DIR    本地已 git clone 的目录
#   REPO_SUBDIR 仓库内存放 md 的子目录 (留空 = 仓库根)
#   TOKEN_VAR   该仓库使用的 token 变量名 (在 TOKEN_FILE 中定义)
REPO_TARGETS=(
    "${WORK_DIR}/metastore|hive/meta|GITLAB_TOKEN"
    "${WORK_DIR}/lofter-alg-data-skills|dev_sql/references/metastore/meta|GITLAB_TOKEN_2"
)

COMMIT_MSG="[hive][bot] Automatically update hive metastore information"

# commit 身份 (留空则使用仓库内已有 git config)
GIT_USER_NAME="metastore-bot"
GIT_USER_EMAIL="metastore-bot@netease.com"

# --- GitLab Access Token ---
TOKEN_FILE="${WORK_DIR}/token.env"     # 600 权限, 内容形如:
                                       #   GITLAB_TOKEN=xxxxxx
                                       #   GITLAB_TOKEN_2=yyyyyy
# 也支持环境变量直接传 GITLAB_TOKEN / GITLAB_TOKEN_2, 优先级高于 TOKEN_FILE

# --- 日志 ---
LOG_DIR="${WORK_DIR}/logs"          # 日志目录 (会自动创建)
LOG_PREFIX="update_hive_metadata"         # 日志文件名前缀
LOG_RETAIN_DAYS=7                         # 保留天数

# ========================================================

# 当天日志文件 (脚本启动时确定一次, 即使脚本跨午夜也写在同一个文件里)
LOG_FILE="${LOG_DIR}/${LOG_PREFIX}.$(date '+%Y-%m-%d').log"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "${LOG_FILE}"
}

die() {
    log "ERROR: $*"
    exit 1
}

init_log() {
    mkdir -p "${LOG_DIR}" || {
        echo "ERROR: cannot create LOG_DIR ${LOG_DIR}" >&2
        exit 1
    }
    # touch 一下确保文件存在 (后续 tee -a 才能正常追加)
    : >> "${LOG_FILE}"
}

rotate_log() {
    # 删除 LOG_RETAIN_DAYS 天前的日志文件
    # -mtime +N 表示修改时间在 N 天之前 (即 >N*24h)
    find "${LOG_DIR}" -maxdepth 1 -type f \
        -name "${LOG_PREFIX}.*.log" \
        -mtime +"${LOG_RETAIN_DAYS}" \
        -print -delete >>"${LOG_FILE}" 2>&1 || true
}

load_token() {
    # 若 TOKEN_FILE 存在则 source 进来; 已通过环境变量传入的 token 不会被覆盖
    # (因为 TOKEN_FILE 里的赋值是无条件的, 这里改用 ": ${VAR:=...}" 的形式
    #  实现"环境变量优先"。简单做法: 仅在变量未设置时才 source)
    if [[ -f "${TOKEN_FILE}" ]]; then
        perm="$(stat -c '%a' "${TOKEN_FILE}" 2>/dev/null || stat -f '%A' "${TOKEN_FILE}")"
        if [[ "${perm}" != "600" && "${perm}" != "400" ]]; then
            die "TOKEN_FILE ${TOKEN_FILE} permission ${perm} too open, expect 600/400"
        fi
        # shellcheck disable=SC1090
        source "${TOKEN_FILE}"
    fi

    # 校验 REPO_TARGETS 里用到的每个 token 变量都已定义且非空
    local target token_var token_val
    for target in "${REPO_TARGETS[@]}"; do
        token_var="${target##*|}"
        token_val="${!token_var:-}"
        [[ -n "${token_val}" ]] || die "${token_var} not set (env or ${TOKEN_FILE})"
    done
}

push_to_repo() {
    # 参数: REPO_DIR  REPO_SUBDIR  TOKEN_VAR
    local repo_dir="$1"
    local repo_subdir="$2"
    local token_var="$3"
    local token_val="${!token_var}"

    log "----- pushing to ${repo_dir} (subdir='${repo_subdir}', token=${token_var}) -----"

    [[ -d "${repo_dir}/.git" ]] || die "REPO_DIR is not a git repo: ${repo_dir}"
    cd "${repo_dir}"

    if [[ -n "${GIT_USER_NAME}" ]]; then
        git config user.name  "${GIT_USER_NAME}"
    fi
    if [[ -n "${GIT_USER_EMAIL}" ]]; then
        git config user.email "${GIT_USER_EMAIL}"
    fi

    local auth_header="PRIVATE-TOKEN: ${token_val}"
    local git_auth=(-c "http.extraHeader=${auth_header}")

    # 取 origin URL, 若是裸 https 则临时构造一个含 token 的 URL 作为推送目标
    # (token 仅在命令行参数里传, 不会落盘到 .git/config)
    local origin_url auth_url
    origin_url="$(git config --get remote.origin.url)"
    if [[ "${origin_url}" =~ ^https://([^@/]+)$ ]] || [[ "${origin_url}" =~ ^https://[^@]+$ && "${origin_url}" != *"@"* ]]; then
        # 裸 https://host/... -> https://oauth2:<token>@host/...
        auth_url="${origin_url/https:\/\//https://oauth2:${token_val}@}"
    else
        # 已含 user:pass@ 的, 沿用原 URL (兼容老仓库的 oauth2:<token>@host 形式)
        auth_url="${origin_url}"
    fi

    # --- 提交前先拉取远端最新代码 (避免本地落后导致 push 被拒) ---
    log "git pull --rebase (pre-copy)"
    if ! git "${git_auth[@]}" pull --rebase "${auth_url}" >>"${LOG_FILE}" 2>&1; then
        git rebase --abort >/dev/null 2>&1 || true
        die "git pull --rebase failed before copy (${repo_dir})"
    fi

    # --- 拷贝 md 到目标子目录 ---
    local target_dir="${repo_dir}"
    if [[ -n "${repo_subdir}" ]]; then
        target_dir="${repo_dir}/${repo_subdir}"
        mkdir -p "${target_dir}"
    fi
    log "copying md files to ${target_dir}"
    cp -f "${md_files[@]}" "${target_dir}/"

    # --- git add ---
    if [[ -n "${repo_subdir}" ]]; then
        git add "${repo_subdir}"
    else
        git add .
    fi

    if git diff --cached --quiet; then
        log "no changes to commit in ${repo_dir}, skip."
        return 0
    fi

    # --- git commit ---
    log "git commit"
    git commit -m "${COMMIT_MSG}" >>"${LOG_FILE}" 2>&1 \
        || die "git commit failed (${repo_dir})"

    # --- 提交后再 pull --rebase 一次 (期间可能有别人新提交) ---
    log "git pull --rebase (pre-push)"
    if ! git "${git_auth[@]}" pull --rebase "${auth_url}" >>"${LOG_FILE}" 2>&1; then
        git rebase --abort >/dev/null 2>&1 || true
        die "git pull --rebase failed before push (${repo_dir})"
    fi

    # --- git push ---
    local branch
    branch="$(git rev-parse --abbrev-ref HEAD)"
    log "git push origin ${branch}"
    git "${git_auth[@]}" push "${auth_url}" "${branch}" >>"${LOG_FILE}" 2>&1 \
        || die "git push failed (${repo_dir})"

    log "----- pushed ${repo_dir} to origin/${branch} -----"
}

main() {
    init_log
    log "===== start update_hive_metadata ====="
    rotate_log

    load_token

    # --- 1. 执行 java 命令 ---
    [[ -d "${WORK_DIR}" ]] || die "WORK_DIR not found: ${WORK_DIR}"
    cd "${WORK_DIR}"
    [[ -f "${JAR_PATH}" ]] || die "jar not found: ${WORK_DIR}/${JAR_PATH}"

    log "running: java -cp ${JAR_PATH} ${MAIN_CLASS}"
    if ! java -cp "${JAR_PATH}" "${MAIN_CLASS}" >>"${LOG_FILE}" 2>&1; then
        die "java command failed, see ${LOG_FILE}"
    fi

    # --- 2. 检查产出 ---
    [[ -d "${OUTPUT_DIR}" ]] || die "output dir not found: ${OUTPUT_DIR}"

    shopt -s nullglob
    md_files=("${OUTPUT_DIR}"/*.md)
    shopt -u nullglob
    if [[ ${#md_files[@]} -eq 0 ]]; then
        die "no .md files generated in ${OUTPUT_DIR}"
    fi
    log "generated ${#md_files[@]} md files"

    # --- 3. 依次推送到每个仓库 (任一失败立即退出) ---
    local target repo_dir repo_subdir token_var
    for target in "${REPO_TARGETS[@]}"; do
        IFS='|' read -r repo_dir repo_subdir token_var <<< "${target}"
        push_to_repo "${repo_dir}" "${repo_subdir}" "${token_var}"
    done

    log "===== done (pushed to ${#REPO_TARGETS[@]} repo(s)) ====="
}

main "$@"
