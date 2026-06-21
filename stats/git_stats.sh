#!/bin/bash
# stats/git_stats.sh
# Recopila estadísticas de commits y issues, genera historial y README

# Koloreak
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
BLUE='\033[1;34m'
CYAN='\033[0;36m'
MAGENTA='\033[1;35m'
RESET='\033[0m'

HISTORY_FILE="stats/history.jsonl"
README_FILE="stats/README.md"

# Cuentas de bot a excluir
BOT_ACCOUNTS=("github-classroom[bot]" "GitHub Action Bot")

# Función para escapar strings en JSON
json_escape() {
  local string="$1"
  # Escapar backslashes, comillas y caracteres especiales
  string="${string//\\/\\\\}"  # \ -> \\
  string="${string//\"/\\\"}"  # " -> \"
  string="${string//$'\n'/\\n}"  # newline -> \n
  string="${string//$'\r'/\\r}"  # carriage return -> \r
  string="${string//$'\t'/\\t}"  # tab -> \t
  echo "$string"
}

# Función para verificar si un usuario es un bot
is_bot_account() {
  local user="$1"
  for bot in "${BOT_ACCOUNTS[@]}"; do
    if [ "$user" = "$bot" ]; then
      return 0
    fi
  done
  return 1
}

# Función para obtener teams del repositorio y sus miembros
get_repo_team_members() {
  local repo_path="$1"
  local token="$2"
  
  if [ -z "$token" ] || [ -z "$repo_path" ]; then
    echo "[]"
    return
  fi
  
  local org=$(echo "$repo_path" | cut -d'/' -f1)
  local repo=$(echo "$repo_path" | cut -d'/' -f2)
  
  echo -e "${CYAN}Consultando teams del repositorio...${RESET}" >&2
  
  # Obtener teams del repositorio
  local teams=$(curl -s -H "Authorization: Bearer $token" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/repos/$repo_path/teams" 2>/dev/null)
  
  if ! echo "$teams" | jq empty 2>/dev/null; then
    echo -e "${YELLOW}  No se pudieron obtener teams (¿falta permiso?)${RESET}" >&2
    echo "[]"
    return
  fi
  
  # Array para acumular todos los miembros
  local all_members="[]"
  
  # Para cada team, obtener sus miembros
  local team_names=$(echo "$teams" | jq -r '.[].name' 2>/dev/null)
  local team_slugs=$(echo "$teams" | jq -r '.[].slug' 2>/dev/null)
  
  # Convertir a arrays
  local names_array=()
  local slugs_array=()
  
  while IFS= read -r name; do
    [ -n "$name" ] && names_array+=("$name")
  done <<< "$team_names"
  
  while IFS= read -r slug; do
    [ -n "$slug" ] && slugs_array+=("$slug")
  done <<< "$team_slugs"
  
  # Procesar cada team
  for i in "${!slugs_array[@]}"; do
    local slug="${slugs_array[$i]}"
    local name="${names_array[$i]}"
    
    if [ -n "$slug" ] && [ "$slug" != "null" ]; then
      echo -e "${CYAN}  Team: $name${RESET}" >&2
      
      local members=$(curl -s -H "Authorization: Bearer $token" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/orgs/$org/teams/$slug/members" 2>/dev/null)
      
      if echo "$members" | jq empty 2>/dev/null; then
        # Mostrar miembros
        local member_logins=$(echo "$members" | jq -r '.[].login' 2>/dev/null)
        while IFS= read -r login; do
          [ -n "$login" ] && echo -e "${CYAN}    - @$login${RESET}" >&2
        done <<< "$member_logins"
        
        # Combinar con all_members
        all_members=$(echo "$all_members" "$members" | jq -s 'add | unique_by(.login)')
      fi
    fi
  done
  
  echo "$all_members"
}

# Función para obtener admins/owners de la organización
get_org_admins() {
  local org="$1"
  local token="$2"
  
  if [ -z "$token" ] || [ -z "$org" ]; then
    echo "[]"
    return
  fi
  
  echo -e "${CYAN}Consultando admins de la organización...${RESET}" >&2
  
  local admins=$(curl -s -H "Authorization: Bearer $token" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/orgs/$org/members?role=admin&per_page=100" 2>/dev/null)
  
  if echo "$admins" | jq empty 2>/dev/null; then
    echo "$admins"
  else
    echo -e "${YELLOW}  No se pudieron obtener admins${RESET}" >&2
    echo "[]"
  fi
}

# Función para obtener información de un usuario (nombre del perfil)
get_user_info() {
  local login="$1"
  local token="$2"
  
  if [ -z "$token" ] || [ -z "$login" ]; then
    echo "{}"
    return
  fi
  
  local user_info=$(curl -s -H "Authorization: Bearer $token" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/users/$login" 2>/dev/null)
  
  if echo "$user_info" | jq empty 2>/dev/null; then
    echo "$user_info"
  else
    echo "{}"
  fi
}

# Mapas globales para normalización
declare -A user_login_map
declare -A user_name_map

# Función para construir mapas de usuarios (nombre <-> login)
build_user_maps() {
  local students_json="$1"
  local teachers_json="$2"
  local token="$3"
  
  echo -e "${CYAN}Construyendo mapa de usuarios...${RESET}"
  
  # Combinar estudiantes y profesores
  local all_users=$(echo "$students_json" "$teachers_json" | jq -s 'add | unique_by(.login)' 2>/dev/null || echo "[]")
  
  local logins=$(echo "$all_users" | jq -r '.[].login' 2>/dev/null)
  
  while IFS= read -r login; do
    if [ -n "$login" ] && [ "$login" != "null" ]; then
      # Mapear login a sí mismo
      user_login_map["$login"]="$login"
      
      # Obtener información del usuario
      local user_info=$(get_user_info "$login" "$token")
      local name=$(echo "$user_info" | jq -r '.name // empty' 2>/dev/null)
      
      # Si tiene nombre, mapear nombre -> login
      if [ -n "$name" ] && [ "$name" != "null" ]; then
        user_login_map["$name"]="$login"
        user_name_map["$login"]="$name"
        echo -e "${CYAN}  Mapeado: '$name' -> @$login${RESET}"
      fi
    fi
  done <<< "$logins"
}

# Función para normalizar un nombre de autor a su login
normalize_author() {
  local author="$1"
  
  # Si está en el mapa, devolver el login
  if [ -n "${user_login_map[$author]}" ]; then
    echo "${user_login_map[$author]}"
  else
    # Si no está en el mapa, devolver el autor original
    echo "$author"
  fi
}

# Función para contar issues en una fecha específica (usa cache)
count_issues_at_date() {
  local target_date="$1"
  local all_issues_json="$2"
  
  if [ -z "$all_issues_json" ]; then
    echo "0,0,0,0"
    return
  fi
  
  # Convertir fecha a formato ISO para comparación
  local target_datetime="${target_date}T23:59:59Z"
  
  # Contar issues abiertas en esa fecha
  local open_total=$(echo "$all_issues_json" | jq --arg date "$target_datetime" '[
    .[] | 
    select(has("pull_request") | not) |
    select(.created_at <= $date) |
    select(.closed_at == null or .closed_at > $date)
  ] | length' 2>/dev/null || echo "0")
  
  # Contar issues abiertas SIN asignar (assignees vacío Y assignee null)
  local open_unassigned=$(echo "$all_issues_json" | jq --arg date "$target_datetime" '[
    .[] | 
    select(has("pull_request") | not) |
    select(.created_at <= $date) |
    select(.closed_at == null or .closed_at > $date) |
    select((.assignees | length) == 0 and .assignee == null)
  ] | length' 2>/dev/null || echo "0")
  
  local open_assigned=$((open_total - open_unassigned))
  
  # Contar issues cerradas en esa fecha
  local closed_total=$(echo "$all_issues_json" | jq --arg date "$target_datetime" '[
    .[] | 
    select(has("pull_request") | not) |
    select(.created_at <= $date) |
    select(.closed_at != null and .closed_at <= $date)
  ] | length' 2>/dev/null || echo "0")
  
  # Contar issues cerradas SIN asignar (assignees vacío Y assignee null)
  local closed_unassigned=$(echo "$all_issues_json" | jq --arg date "$target_datetime" '[
    .[] | 
    select(has("pull_request") | not) |
    select(.created_at <= $date) |
    select(.closed_at != null and .closed_at <= $date) |
    select((.assignees | length) == 0 and .assignee == null)
  ] | length' 2>/dev/null || echo "0")
  
  local closed_assigned=$((closed_total - closed_unassigned))
  
  # Contar issues con tags (creadas hasta esa fecha)
  local with_tags=$(echo "$all_issues_json" | jq --arg date "$target_datetime" '[
    .[] | 
    select(has("pull_request") | not) |
    select(.created_at <= $date) |
    select((.labels | length) > 0)
  ] | length' 2>/dev/null || echo "0")
  
  # Contar issues sin tags (creadas hasta esa fecha)
  local without_tags=$(echo "$all_issues_json" | jq --arg date "$target_datetime" '[
    .[] | 
    select(has("pull_request") | not) |
    select(.created_at <= $date) |
    select((.labels | length) == 0)
  ] | length' 2>/dev/null || echo "0")
  
  echo "$open_total,$closed_total,$open_assigned,$open_unassigned,$closed_assigned,$closed_unassigned,$with_tags,$without_tags"
}

mkdir -p stats
touch "$HISTORY_FILE"

# Fecha actual
NOW=$(date "+%Y-%m-%d %H:%M:%S")

# --- OBTENER INFORMACIÓN DEL REPOSITORIO Y USUARIOS ---
echo -e "${BLUE}=== Obteniendo información del repositorio y organización ===${RESET}"

# Obtener información del repositorio desde git remote o variable de entorno
if [ -n "$GITHUB_REPOSITORY" ]; then
  REPO_PATH="$GITHUB_REPOSITORY"
else
  REPO_URL=$(git config --get remote.origin.url)
  REPO_PATH=$(echo "$REPO_URL" | sed -E 's/.*github\.com[:/]([^/]+\/[^/]+)(\.git)?$/\1/')
fi

# Extraer organización del REPO_PATH
ORG_NAME=$(echo "$REPO_PATH" | cut -d'/' -f1)

echo -e "${CYAN}Organización detectada: $ORG_NAME${RESET}"
echo -e "${CYAN}Repositorio: $REPO_PATH${RESET}"

# Determinar qué token usar (ORG_TOKEN tiene prioridad, si no existe usar GH_TOKEN)
API_TOKEN="${ORG_TOKEN:-$GH_TOKEN}"

if [ -z "$API_TOKEN" ]; then
  echo -e "${YELLOW}⚠️ No se encontró ORG_TOKEN ni GH_TOKEN${RESET}"
  echo -e "${YELLOW}   No se podrá filtrar por estudiantes/profesores${RESET}"
  STUDENTS_LOGINS=""
  TEACHERS_LOGINS=""
else
  if [ -n "$ORG_TOKEN" ]; then
    echo -e "${GREEN}Token encontrado: ORG_TOKEN${RESET}"
  fi
  if [ -n "$GH_TOKEN" ]; then
    echo -e "${GREEN}Token encontrado: GH_TOKEN${RESET}"
  fi
  
  # Obtener estudiantes (miembros de teams del repositorio)
  STUDENTS_JSON=$(get_repo_team_members "$REPO_PATH" "$API_TOKEN")
  STUDENTS_LOGINS=$(echo "$STUDENTS_JSON" | jq -r '.[].login' 2>/dev/null || echo "")

  # Obtener profesores (admins de la organización)
  TEACHERS_JSON=$(get_org_admins "$ORG_NAME" "$API_TOKEN")
  TEACHERS_LOGINS=$(echo "$TEACHERS_JSON" | jq -r '.[].login' 2>/dev/null || echo "")

  # Construir mapas de usuarios (nombre <-> login)
  build_user_maps "$STUDENTS_JSON" "$TEACHERS_JSON" "$API_TOKEN"
fi

echo -e "${GREEN}=== Estudiantes (members) ===${RESET}"
if [ -n "$STUDENTS_LOGINS" ] && [ "$STUDENTS_LOGINS" != "null" ]; then
  while IFS= read -r student; do
    if [ -n "$student" ] && [ "$student" != "null" ]; then
      display_name="${user_name_map[$student]}"
      if [ -n "$display_name" ]; then
        echo -e "${CYAN}  @$student ($display_name)${RESET}"
      else
        echo -e "${CYAN}  @$student${RESET}"
      fi
    fi
  done <<< "$STUDENTS_LOGINS"
else
  echo -e "${YELLOW}  No se pudieron obtener estudiantes${RESET}"
fi

echo -e "${GREEN}=== Profesores (admins/owners) ===${RESET}"
if [ -n "$TEACHERS_LOGINS" ] && [ "$TEACHERS_LOGINS" != "null" ]; then
  while IFS= read -r teacher; do
    if [ -n "$teacher" ] && [ "$teacher" != "null" ]; then
      display_name="${user_name_map[$teacher]}"
      if [ -n "$display_name" ]; then
        echo -e "${MAGENTA}  @$teacher ($display_name)${RESET}"
      else
        echo -e "${MAGENTA}  @$teacher${RESET}"
      fi
    fi
  done <<< "$TEACHERS_LOGINS"
else
  echo -e "${YELLOW}  No se pudieron obtener profesores${RESET}"
fi

# --- REGENERAR HISTORIAL COMPLETO EN CADA EJECUCIÓN ---
echo -e "${YELLOW}=== Regenerando historial completo con datos históricos... ===${RESET}"

# Eliminar historial existente para regenerarlo completamente
if [ -f "$HISTORY_FILE" ]; then
  echo -e "${CYAN}  Eliminando historial anterior...${RESET}"
  rm -f "$HISTORY_FILE"
fi

# Crear archivo vacío
touch "$HISTORY_FILE"
  
  # Obtener TODAS las issues de una sola vez para optimizar
  if [ -n "$API_TOKEN" ] && [ -n "$REPO_PATH" ]; then
    echo -e "${CYAN}Obteniendo todas las issues del repositorio...${RESET}"
    ALL_ISSUES_CACHE=$(curl -s -H "Authorization: Bearer $API_TOKEN" \
      -H "Accept: application/vnd.github.v3+json" \
      "https://api.github.com/repos/$REPO_PATH/issues?state=all&per_page=100&direction=asc")
    
    if ! echo "$ALL_ISSUES_CACHE" | jq empty 2>/dev/null; then
      echo -e "${YELLOW}⚠ No se pudieron obtener issues, continuando sin datos de issues históricos${RESET}"
      ALL_ISSUES_CACHE=""
    fi
  else
    ALL_ISSUES_CACHE=""
  fi
  
  # Obtener la fecha del primer commit
  FIRST_COMMIT_DATE=$(git log --reverse --format="%ai" | head -n1 | cut -d' ' -f1)
  
  if [ -n "$FIRST_COMMIT_DATE" ]; then
    echo -e "${CYAN}Primer commit encontrado: $FIRST_COMMIT_DATE${RESET}"
    
    # Convertir fecha a timestamp
    FIRST_TIMESTAMP=$(date -d "$FIRST_COMMIT_DATE" +%s 2>/dev/null || date -j -f "%Y-%m-%d" "$FIRST_COMMIT_DATE" +%s 2>/dev/null)
    CURRENT_TIMESTAMP=$(date +%s)
    
    # Calcular timestamp de AYER (excluir hoy del bucle histórico)
    YESTERDAY_TIMESTAMP=$((CURRENT_TIMESTAMP - 86400))
    
    # Generar entradas DIARIAS desde el primer commit hasta AYER (no hoy)
    DAY_TIMESTAMP=$FIRST_TIMESTAMP
    
    while [ $DAY_TIMESTAMP -le $YESTERDAY_TIMESTAMP ]; do
      # Fecha de esta iteración (23:59:59 para indicar que incluye todo el día)
      ITER_DATE=$(date -d "@$DAY_TIMESTAMP" "+%Y-%m-%d 23:59:59" 2>/dev/null || date -r $DAY_TIMESTAMP "+%Y-%m-%d 23:59:59" 2>/dev/null)
      ITER_DATE_ONLY=$(echo "$ITER_DATE" | cut -d' ' -f1)
      
      echo -e "${CYAN}  Procesando: $ITER_DATE_ONLY${RESET}"
      
      # Contar commits SOLO de este día específico (no acumulativo)
      declare -A user_commits_hist
      
      # Calcular el día anterior para el rango
      PREV_DAY_TIMESTAMP=$((DAY_TIMESTAMP - 86400))
      PREV_DATE=$(date -d "@$PREV_DAY_TIMESTAMP" "+%Y-%m-%d" 2>/dev/null || date -r $PREV_DAY_TIMESTAMP "+%Y-%m-%d" 2>/dev/null)
      
      # Obtener lista de autores únicos en este día específico
      authors=$(git log --all --after="$PREV_DATE 23:59:59" --before="$ITER_DATE_ONLY 23:59:59" --format='%aN' | sort -u)
      
      # Procesar cada autor y contar sus commits DE ESTE DÍA
      while IFS= read -r author; do
        if [ -n "$author" ]; then
          # Normalizar el autor a su login
          normalized_author=$(normalize_author "$author")
          
          # Verificar si es un bot
          if is_bot_account "$author" || is_bot_account "$normalized_author"; then
            continue
          fi
          
          # Verificar si es estudiante
          is_student=false
          if [ -n "$STUDENTS_LOGINS" ]; then
            while IFS= read -r student; do
              if [ "$normalized_author" = "$student" ]; then
                is_student=true
                break
              fi
            done <<< "$STUDENTS_LOGINS"
          fi
          
          # Solo contar si es estudiante
          if [ "$is_student" = true ]; then
            # Contar commits de este autor EN ESTE DÍA ESPECÍFICO
            count=$(git log --all --after="$PREV_DATE 23:59:59" --before="$ITER_DATE_ONLY 23:59:59" --author="$author" --oneline 2>/dev/null | wc -l || echo "0")
            
            # Acumular en el array asociativo bajo el login normalizado
            if [ -n "${user_commits_hist[$normalized_author]}" ]; then
              user_commits_hist["$normalized_author"]=$((${user_commits_hist[$normalized_author]} + count))
            else
              user_commits_hist["$normalized_author"]=$count
            fi
          fi
        fi
      done <<< "$authors"
      
      # Construir JSON desde el array asociativo
      commits_by_user_json="{"
      first_user=true
      
      # Construir JSON desde el array asociativo
      for norm_user in "${!user_commits_hist[@]}"; do
        if [ "$first_user" = true ]; then
          first_user=false
        else
          commits_by_user_json+=","
        fi
        user_escaped=$(json_escape "$norm_user")
        commits_by_user_json+="\"$user_escaped\":${user_commits_hist[$norm_user]}"
      done
      commits_by_user_json+="}"
      
      # Calcular total de commits (suma de todos los usuarios)
      total_at_date=0
      for key in "${!user_commits_hist[@]}"; do
        total_at_date=$((total_at_date + ${user_commits_hist[$key]}))
      done
      
      # Contar commits por rama hasta esta fecha (solo de estudiantes)
      branches=$(git branch -r | grep -v HEAD | sed 's/^[ \t]*//' | sed 's/origin\///' | grep -v '^$' | sort -u)
      
      declare -A branch_commits_hist
      
      while IFS= read -r branch; do
        # Limpiar espacios y validar que sea un nombre de rama válido
        branch=$(echo "$branch" | xargs | tr -d '\r\n')
        
        # Saltar si está vacío o contiene caracteres raros
        if [ -z "$branch" ] || [[ "$branch" =~ [[:space:]] ]] || [[ "$branch" == *"["* ]]; then
          continue
        fi
        
        # Verificar que la rama existe
        if ! git rev-parse --verify "origin/$branch" >/dev/null 2>&1; then
          continue
        fi
        
        # Obtener commits acumulativos de la rama HASTA ESTA FECHA (23:59:59)
        branch_commit_hashes=$(git rev-list "origin/$branch" --before="$ITER_DATE_ONLY 23:59:59" 2>/dev/null)
        
        count=0
        while IFS= read -r commit_hash; do
          if [ -n "$commit_hash" ]; then
            # Obtener autor del commit
            author=$(git show -s --format='%aN' "$commit_hash" 2>/dev/null)
            
            if [ -n "$author" ]; then
              normalized_author=$(normalize_author "$author")
              
              # Verificar si es bot
              if is_bot_account "$author" || is_bot_account "$normalized_author"; then
                continue
              fi
              
              # Verificar si es estudiante
              is_student=false
              if [ -n "$STUDENTS_LOGINS" ]; then
                while IFS= read -r student; do
                  if [ "$normalized_author" = "$student" ]; then
                    is_student=true
                    break
                  fi
                done <<< "$STUDENTS_LOGINS"
              fi
              
              if [ "$is_student" = true ]; then
                count=$((count + 1))
              fi
            fi
          fi
        done <<< "$branch_commit_hashes"
        
        if [ "$count" -gt 0 ]; then
          branch_commits_hist["$branch"]=$count
        fi
      done <<< "$branches"
      
      # Construir JSON de ramas
      commits_by_branch_json="{"
      first_branch=true
      for branch in "${!branch_commits_hist[@]}"; do
        if [ "$first_branch" = true ]; then
          first_branch=false
        else
          commits_by_branch_json+=","
        fi
        branch_escaped=$(json_escape "$branch")
        commits_by_branch_json+="\"$branch_escaped\":${branch_commits_hist[$branch]}"
      done
      commits_by_branch_json+="}"
      
      # Calcular issues para esta fecha histórica usando el cache
      hist_open_total=0
      hist_closed_total=0
      hist_open_assigned=0
      hist_open_unassigned=0
      hist_closed_assigned=0
      hist_closed_unassigned=0
      hist_with_tags=0
      hist_without_tags=0
      
      if [ -n "$ALL_ISSUES_CACHE" ]; then
        ISSUE_COUNTS=$(count_issues_at_date "$ITER_DATE_ONLY" "$ALL_ISSUES_CACHE")
        hist_open_total=$(echo "$ISSUE_COUNTS" | cut -d',' -f1)
        hist_closed_total=$(echo "$ISSUE_COUNTS" | cut -d',' -f2)
        hist_open_assigned=$(echo "$ISSUE_COUNTS" | cut -d',' -f3)
        hist_open_unassigned=$(echo "$ISSUE_COUNTS" | cut -d',' -f4)
        hist_closed_assigned=$(echo "$ISSUE_COUNTS" | cut -d',' -f5)
        hist_closed_unassigned=$(echo "$ISSUE_COUNTS" | cut -d',' -f6)
        hist_with_tags=$(echo "$ISSUE_COUNTS" | cut -d',' -f7)
        hist_without_tags=$(echo "$ISSUE_COUNTS" | cut -d',' -f8)
      fi
      
      # Crear registro histórico (con issues calculadas según timestamps)
      HIST_RECORD="{\"date\": \"$ITER_DATE\", \"commits_total\": $total_at_date, \"commits_by_user\": $commits_by_user_json, \"commits_by_branch\": $commits_by_branch_json, \"issues\": {\"open\": $hist_open_total, \"closed\": $hist_closed_total, \"open_assigned\": $hist_open_assigned, \"open_unassigned\": $hist_open_unassigned, \"closed_assigned\": $hist_closed_assigned, \"closed_unassigned\": $hist_closed_unassigned, \"with_tags\": $hist_with_tags, \"without_tags\": $hist_without_tags}}"
      
      # Validar JSON antes de añadirlo
      if echo "$HIST_RECORD" | jq empty 2>/dev/null; then
        echo "$HIST_RECORD" >> "$HISTORY_FILE"
      else
        echo -e "${YELLOW}⚠ Línea JSON inválida para fecha $ITER_DATE_ONLY (saltando)${RESET}"
      fi
      
      # Avanzar UN DÍA (86400 segundos = 24 horas)
      DAY_TIMESTAMP=$((DAY_TIMESTAMP + 86400))
    done
    
    HIST_ENTRIES=$(wc -l < "$HISTORY_FILE")
    echo -e "${GREEN}✓ Historial regenerado con $HIST_ENTRIES entradas (todas con timestamp 23:59:59)${RESET}"
  else
    echo -e "${YELLOW}No se encontraron commits en el repositorio${RESET}"
  fi

echo -e "${BLUE}=== Recopilando estadísticas del día actual ===${RESET}"

# --- RECOPILAR COMMITS POR USUARIO (SOLO ESTUDIANTES) ---
declare -A user_commits
commits_total=0

# Obtener lista de autores únicos
authors=$(git log --all --format='%aN' | sort -u)

while IFS= read -r author; do
  if [ -n "$author" ]; then
    # Normalizar el autor a su login
    normalized_author=$(normalize_author "$author")
    
    # Verificar si es un bot
    if is_bot_account "$author" || is_bot_account "$normalized_author"; then
      echo -e "${YELLOW}  ⊗ $author (bot excluido)${RESET}"
      continue
    fi
    
    # Verificar si es estudiante
    is_student=false
    if [ -n "$STUDENTS_LOGINS" ]; then
      while IFS= read -r student; do
        if [ "$normalized_author" = "$student" ]; then
          is_student=true
          break
        fi
      done <<< "$STUDENTS_LOGINS"
    fi
    
    if [ "$is_student" = true ]; then
      count=$(git log --all --author="$author" --oneline | wc -l)
      
      # Acumular commits bajo el login normalizado
      if [ -n "${user_commits[$normalized_author]}" ]; then
        user_commits["$normalized_author"]=$((${user_commits[$normalized_author]} + count))
      else
        user_commits["$normalized_author"]=$count
      fi
      
      commits_total=$((commits_total + count))
      
      if [ "$author" != "$normalized_author" ]; then
        echo -e "${CYAN}  $author (@$normalized_author): $count commits${RESET}"
      else
        echo -e "${CYAN}  @$author: $count commits${RESET}"
      fi
    else
      echo -e "${YELLOW}  ⊗ $author (no es estudiante, excluido)${RESET}"
    fi
  fi
done <<< "$authors"

echo -e "${GREEN}Total de commits de estudiantes: $commits_total${RESET}"

# --- RECOPILAR ISSUES DE GITHUB ---
echo -e "${BLUE}=== Recopilando issues de GitHub ===${RESET}"

i_open_total=0
i_open_assigned=0
i_open_unassigned=0
i_closed_total=0
i_closed_assigned=0
i_closed_unassigned=0
i_with_tags=0
i_without_tags=0

if [ -n "$API_TOKEN" ] && [ -n "$REPO_PATH" ]; then
  echo -e "${CYAN}Consultando issues del repositorio: $REPO_PATH${RESET}"
  
  # Issues abiertas con manejo de errores mejorado
  OPEN_ISSUES=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $API_TOKEN" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/repos/$REPO_PATH/issues?state=open&per_page=100")
  
  HTTP_CODE=$(echo "$OPEN_ISSUES" | tail -n1)
  OPEN_ISSUES=$(echo "$OPEN_ISSUES" | sed '$d')
  
  echo -e "${CYAN}HTTP Status Code (open): $HTTP_CODE${RESET}"
  
  # Verificar que la respuesta sea JSON válido y exitosa
  if [ "$HTTP_CODE" = "200" ] && echo "$OPEN_ISSUES" | jq empty 2>/dev/null; then
    if [ "$OPEN_ISSUES" != "[]" ] && [ "$OPEN_ISSUES" != "null" ]; then
      # Contar solo issues abiertas totales (sin separar por asignación)
      i_open_total=$(echo "$OPEN_ISSUES" | jq '[.[] | select(has("pull_request") | not)] | length' 2>/dev/null || echo "0")
  # Contar issues sin asignar (assignees array vacío Y assignee es null)
  # Importante: todas las condiciones deben estar dentro del mismo select(...)
  i_open_unassigned=$(echo "$OPEN_ISSUES" | jq '[.[] | select((has("pull_request") | not) and ((.assignees | length) == 0) and (.assignee == null))] | length' 2>/dev/null || echo "0")
      # Calcular asignadas restando
      i_open_assigned=$((i_open_total - i_open_unassigned))
      echo -e "${CYAN}Debug - Issues abiertas: $i_open_total (Asignadas: $i_open_assigned, Sin asignar: $i_open_unassigned)${RESET}"
    fi
  else
    echo -e "${YELLOW}⚠ Error al obtener issues abiertas (HTTP $HTTP_CODE)${RESET}"
    echo -e "${YELLOW}Respuesta: $(echo "$OPEN_ISSUES" | head -c 200)${RESET}"
  fi
  
  # Issues cerradas con manejo de errores mejorado
  CLOSED_ISSUES=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $API_TOKEN" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/repos/$REPO_PATH/issues?state=closed&per_page=100")
  
  HTTP_CODE=$(echo "$CLOSED_ISSUES" | tail -n1)
  CLOSED_ISSUES=$(echo "$CLOSED_ISSUES" | sed '$d')
  
  echo -e "${CYAN}HTTP Status Code (closed): $HTTP_CODE${RESET}"
  
  # Verificar que la respuesta sea JSON válido y exitosa
  if [ "$HTTP_CODE" = "200" ] && echo "$CLOSED_ISSUES" | jq empty 2>/dev/null; then
    if [ "$CLOSED_ISSUES" != "[]" ] && [ "$CLOSED_ISSUES" != "null" ]; then
      # Contar solo issues cerradas totales (sin separar por asignación)
      i_closed_total=$(echo "$CLOSED_ISSUES" | jq '[.[] | select(has("pull_request") | not)] | length' 2>/dev/null || echo "0")
  # Contar issues sin asignar (assignees array vacío Y assignee es null)
  # Importante: todas las condiciones deben estar dentro del mismo select(...)
  i_closed_unassigned=$(echo "$CLOSED_ISSUES" | jq '[.[] | select((has("pull_request") | not) and ((.assignees | length) == 0) and (.assignee == null))] | length' 2>/dev/null || echo "0")
      # Calcular asignadas restando
      i_closed_assigned=$((i_closed_total - i_closed_unassigned))
      echo -e "${CYAN}Debug - Issues cerradas: $i_closed_total (Asignadas: $i_closed_assigned, Sin asignar: $i_closed_unassigned)${RESET}"
    fi
  else
    echo -e "${YELLOW}⚠ Error al obtener issues cerradas (HTTP $HTTP_CODE)${RESET}"
    echo -e "${YELLOW}Respuesta: $(echo "$CLOSED_ISSUES" | head -c 200)${RESET}"
  fi
  
  # Contar issues con/sin tags (labels)
  # Combinar todas las issues (abiertas + cerradas)
  ALL_ISSUES_COMBINED="$OPEN_ISSUES"
  if [ "$CLOSED_ISSUES" != "[]" ] && [ "$CLOSED_ISSUES" != "null" ] && [ -n "$CLOSED_ISSUES" ]; then
    if [ "$ALL_ISSUES_COMBINED" = "[]" ] || [ "$ALL_ISSUES_COMBINED" = "null" ] || [ -z "$ALL_ISSUES_COMBINED" ]; then
      ALL_ISSUES_COMBINED="$CLOSED_ISSUES"
    else
      # Combinar ambos arrays JSON
      ALL_ISSUES_COMBINED=$(echo "$OPEN_ISSUES" "$CLOSED_ISSUES" | jq -s 'add')
    fi
  fi
  
  # Contar issues con tags (al menos un label)
  i_with_tags=$(echo "$ALL_ISSUES_COMBINED" | jq '[.[] | select(has("pull_request") | not) | select((.labels | length) > 0)] | length' 2>/dev/null || echo "0")
  
  # Contar issues sin tags (labels array vacío)
  i_without_tags=$(echo "$ALL_ISSUES_COMBINED" | jq '[.[] | select(has("pull_request") | not) | select((.labels | length) == 0)] | length' 2>/dev/null || echo "0")
  
  echo -e "${GREEN}Issues abiertas: $i_open_total${RESET}"
  echo -e "${GREEN}Issues cerradas: $i_closed_total${RESET}"
  echo -e "${GREEN}Issues con tags: $i_with_tags${RESET}"
  echo -e "${GREEN}Issues sin tags: $i_without_tags${RESET}"
else
  echo -e "${YELLOW}No se puede consultar GitHub API (falta token o repositorio no detectado)${RESET}"
  [ -z "$API_TOKEN" ] && echo -e "${YELLOW}  - ORG_TOKEN/GH_TOKEN no está definido${RESET}"
  [ -z "$REPO_PATH" ] && echo -e "${YELLOW}  - REPO_PATH no se pudo detectar${RESET}"
fi

# Calcular totales de issues
issues_total_open=$i_open_total
issues_total_closed=$i_closed_total
# Calcular totales de issues asignadas/sin asignar (suma de abiertas + cerradas de cada tipo)
issues_total_assigned=$((i_open_assigned + i_closed_assigned))
issues_total_unassigned=$((i_open_unassigned + i_closed_unassigned))

# Mostrar resumen claro de asignación para la salida del workflow
echo -e "${BLUE}=== Resumen de Issues (asignación) ===${RESET}"
echo -e "${GREEN}  Asignadas (total): ${issues_total_assigned}${RESET}"
echo -e "${GREEN}  Sin asignar (total): ${issues_total_unassigned}${RESET}"
echo -e "${CYAN}  Detalle abiertas -> Asignadas: ${i_open_assigned}, Sin asignar: ${i_open_unassigned}${RESET}"
echo -e "${CYAN}  Detalle cerradas -> Asignadas: ${i_closed_assigned}, Sin asignar: ${i_closed_unassigned}${RESET}"

# --- RECOPILAR COMMITS POR RAMA (SOLO ESTUDIANTES) ---
echo -e "${BLUE}=== Recopilando estadísticas por rama ===${RESET}"

# Obtener fecha de hoy a las 23:59:59 para el límite
TODAY_DATE=$(date "+%Y-%m-%d 23:59:59")

declare -A branch_commits
# Obtener todas las ramas remotas, sin duplicados
branches=$(git branch -r | grep -v HEAD | sed 's/^[ \t]*//' | sed 's/origin\///' | grep -v '^$' | sort -u)

while IFS= read -r branch; do
  # Limpiar espacios y validar que sea un nombre de rama válido
  branch=$(echo "$branch" | xargs | tr -d '\r\n')
  
  # Saltar si está vacío o contiene caracteres raros
  if [ -z "$branch" ] || [[ "$branch" =~ [[:space:]] ]] || [[ "$branch" == *"["* ]]; then
    continue
  fi
  
  # Verificar que la rama existe
  if ! git rev-parse --verify "origin/$branch" >/dev/null 2>&1; then
    continue
  fi
  
  # Obtener commits únicos de la rama hasta las 23:59:59 de HOY (acumulativo hasta hoy)
  branch_commit_hashes=$(git rev-list "origin/$branch" --before="$TODAY_DATE" 2>/dev/null)
  
  count=0
  while IFS= read -r commit_hash; do
    if [ -n "$commit_hash" ]; then
      # Obtener autor del commit
      author=$(git show -s --format='%aN' "$commit_hash" 2>/dev/null)
      
      if [ -n "$author" ]; then
        normalized_author=$(normalize_author "$author")
        
        # Verificar si es bot
        if is_bot_account "$author" || is_bot_account "$normalized_author"; then
          continue
        fi
        
        # Verificar si es estudiante
        is_student=false
        if [ -n "$STUDENTS_LOGINS" ]; then
          while IFS= read -r student; do
            if [ "$normalized_author" = "$student" ]; then
              is_student=true
              break
            fi
          done <<< "$STUDENTS_LOGINS"
        fi
        
        if [ "$is_student" = true ]; then
          count=$((count + 1))
        fi
      fi
    fi
  done <<< "$branch_commit_hashes"
  
  if [ "$count" -gt 0 ]; then
    branch_commits["$branch"]=$count
    echo -e "${CYAN}  $branch: $count commits de estudiantes${RESET}"
  fi
done <<< "$branches"

# --- CONSTRUIR JSON DE COMMITS POR USUARIO ---
commits_by_user_json="{"
first=true
for u in "${!user_commits[@]}"; do
  if [ "$first" = true ]; then
    first=false
  else
    commits_by_user_json+=","
  fi
  u_escaped=$(json_escape "$u")
  commits_by_user_json+="\"$u_escaped\":${user_commits[$u]}"
done
commits_by_user_json+="}"

# --- CONSTRUIR JSON DE COMMITS POR RAMA ---
commits_by_branch_json="{"
first=true
for b in "${!branch_commits[@]}"; do
  if [ "$first" = true ]; then
    first=false
  else
    commits_by_branch_json+=","
  fi
  b_escaped=$(json_escape "$b")
  commits_by_branch_json+="\"$b_escaped\":${branch_commits[$b]}"
done
commits_by_branch_json+="}"

# --- AÑADIR NUEVO REGISTRO AL HISTORIAL ---
# Extraer solo la fecha (sin hora) para comparación
TODAY_DATE=$(echo "$NOW" | cut -d' ' -f1)

# Construir el JSON de forma más robusta con espacios apropiados
NEW_RECORD=$(cat <<EOF
{"date": "$NOW", "commits_total": $commits_total, "commits_by_user": $commits_by_user_json, "commits_by_branch": $commits_by_branch_json, "issues": {"open": $i_open_total, "closed": $i_closed_total, "open_assigned": $i_open_assigned, "open_unassigned": $i_open_unassigned, "closed_assigned": $i_closed_assigned, "closed_unassigned": $i_closed_unassigned, "with_tags": $i_with_tags, "without_tags": $i_without_tags}}
EOF
)

# Validar que el JSON sea válido antes de añadirlo
if echo "$NEW_RECORD" | jq empty 2>/dev/null; then
  echo "$NEW_RECORD" >> "$HISTORY_FILE"
  echo -e "${GREEN}✅ Registro del día actual añadido al historial: $NOW${RESET}"
else
  echo -e "${YELLOW}⚠ Error: JSON generado no es válido, no se añadió al historial${RESET}"
  echo -e "${YELLOW}   Contenido: $NEW_RECORD${RESET}"
fi

# --- GENERAR README.md ---

cat > "$README_FILE" << 'EOFREADME'
# Errepositorioaren estatistikak
EOFREADME

echo "Azken eguneraketa: $NOW" >> "$README_FILE"
echo "" >> "$README_FILE"

# --- HIRU TAULA ILARA BATEAN ---
echo "<table>" >> "$README_FILE"
echo "<tr>" >> "$README_FILE"

# 1. zutabea: Erabiltzaileka Commits taula
echo "<td valign=\"top\">" >> "$README_FILE"
echo "" >> "$README_FILE"
echo "| Erabiltzailea | Commits-ak |" >> "$README_FILE"
echo "|--------------|---------|" >> "$README_FILE"
for u in "${!user_commits[@]}"; do
  echo "| $u | ${user_commits[$u]} |" >> "$README_FILE"
done
echo "| **Guztira** | **$commits_total** |" >> "$README_FILE"
echo "" >> "$README_FILE"
echo "</td>" >> "$README_FILE"

# 2. zutabea: Adar bakoitzeko Commits taula
echo "<td valign=\"top\">" >> "$README_FILE"
echo "" >> "$README_FILE"
echo "| Adarra | Commits-ak |" >> "$README_FILE"
echo "|--------|---------|" >> "$README_FILE"
# Determinar adar lehenetsia (remote HEAD)
MAIN_BRANCH=$(git symbolic-ref --quiet --short refs/remotes/origin/HEAD 2>/dev/null | sed 's@^origin/@@')
# Fallback-ak
if [ -z "$MAIN_BRANCH" ]; then
  if [[ -n "${branch_commits[main]}" ]]; then MAIN_BRANCH="main"; fi
  if [[ -z "$MAIN_BRANCH" && -n "${branch_commits[master]}" ]]; then MAIN_BRANCH="master"; fi
fi

# Lehenik, adar nagusia baldin badago (fondo koloreduna)
if [ -n "$MAIN_BRANCH" ] && [ -n "${branch_commits[$MAIN_BRANCH]}" ]; then
  echo "| 🌿 **$MAIN_BRANCH** | **${branch_commits[$MAIN_BRANCH]}** |" >> "$README_FILE"
fi

# Ondoren, gainerako adarrak ordenatuta
for b in $(printf "%s\n" "${!branch_commits[@]}" | sort); do
  if [ "$b" != "$MAIN_BRANCH" ]; then
    echo "| $b | ${branch_commits[$b]} |" >> "$README_FILE"
  fi
done
echo "" >> "$README_FILE"
echo "</td>" >> "$README_FILE"

# 3. zutabea: Issues taula
echo "<td valign=\"top\">" >> "$README_FILE"
echo "" >> "$README_FILE"
echo "| 📊 Issues | Guztira |" >> "$README_FILE"
echo "|--------|---------|" >> "$README_FILE"
echo "| 📗 **Irekita** | $issues_total_open |" >> "$README_FILE"
echo "| 📕 **Itxita** | $issues_total_closed |" >> "$README_FILE"
echo "| ➖➖➖ | ➖➖➖ |" >> "$README_FILE"
echo "| 🙋🏻 **Esleituta** | $issues_total_assigned |" >> "$README_FILE"
echo "| 👻 **Esleitu gabe** | $issues_total_unassigned |" >> "$README_FILE"
echo "| ➖➖➖ | ➖➖➖ |" >> "$README_FILE"
echo "| 🏷️ **Etiketekin** | $i_with_tags |" >> "$README_FILE"
echo "| 🚫 **Etiketarik gabe** | $i_without_tags |" >> "$README_FILE"
echo "" >> "$README_FILE"
echo "</td>" >> "$README_FILE"

echo "</tr>" >> "$README_FILE"
echo "</table>" >> "$README_FILE"
echo "" >> "$README_FILE"

# --- AÑADIR SECCIÓN DE PROFESORES ---
if [ -n "$TEACHERS_LOGINS" ] && [ "$TEACHERS_LOGINS" != "null" ]; then
  echo "## 👨‍🏫 Irakasleak / Profesores" >> "$README_FILE"
  echo "" >> "$README_FILE"
  
  while IFS= read -r teacher; do
    if [ -n "$teacher" ] && [ "$teacher" != "null" ]; then
      display_name="${user_name_map[$teacher]}"
      if [ -n "$display_name" ]; then
        echo "- **@$teacher** ($display_name)" >> "$README_FILE"
      else
        echo "- **@$teacher**" >> "$README_FILE"
      fi
    fi
  done <<< "$TEACHERS_LOGINS"
  
  echo "" >> "$README_FILE"
fi

# --- GEHITU GRAFIKOAK READMEra (ORDEN BERRIA) ---
echo "## 📊 Estatistikaren bilakaera" >> "$README_FILE"
echo "" >> "$README_FILE"

# 1. Commits guztira
echo "### 1. Commits-ak Guztira" >> "$README_FILE"
echo "" >> "$README_FILE"
echo "![Commits guztien bilakaera](graphs/commits_total.png)" >> "$README_FILE"
echo "" >> "$README_FILE"

# 2. Erabiltzaileka Commits-ak
echo "### 2. Erabiltzaileka Commits-ak" >> "$README_FILE"
echo "" >> "$README_FILE"
echo "![Erabiltzaileka Commits-en bilakaera](graphs/commits_by_user.png)" >> "$README_FILE"
echo "" >> "$README_FILE"

# 3. Adarka Commits-ak
echo "### 3. Adarka Commits-ak" >> "$README_FILE"
echo "" >> "$README_FILE"
echo "![Adarka Commits-en bilakaera](graphs/commits_by_branch.png)" >> "$README_FILE"
echo "" >> "$README_FILE"

# 4. Asteko jarduera
echo "### 4. Asteko Commits-en Jarduera" >> "$README_FILE"
echo "" >> "$README_FILE"
echo "![Asteko jarduera](graphs/weekly_activity.png)" >> "$README_FILE"
echo "" >> "$README_FILE"

# 5. Erabiltzaileka banaketa
echo "### 5. Erabiltzaileka Commits-en Banaketa" >> "$README_FILE"
echo "" >> "$README_FILE"
echo "![Erabiltzaileka Commits-en banaketa](graphs/commits_distribution.png)" >> "$README_FILE"
echo "" >> "$README_FILE"

# 6. Issues
echo "### 6. Issues (Irekita vs Itxita)" >> "$README_FILE"
echo "" >> "$README_FILE"
echo "![Issues bilakaera](graphs/issues.png)" >> "$README_FILE"
echo "" >> "$README_FILE"

echo -e "${GREEN}README.md sortu da stats/README.md-n${RESET}"
