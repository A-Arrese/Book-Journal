#!/usr/bin/env python3
# stats/generate_graphs.py
# Genera gráficos de commits y issues a partir del historial JSONL

import json
import subprocess
import matplotlib.pyplot as plt
from matplotlib.ticker import MaxNLocator
import os
import numpy as np
from datetime import datetime

HISTORY_FILE = "stats/history.jsonl"

# Crear carpeta para gráficos
os.makedirs("stats/graphs", exist_ok=True)

history = []

# Leer el historial
with open(HISTORY_FILE, "r") as f:
    line_num = 0
    for line in f:
        line_num += 1
        line = line.strip()
        if line:
            try:
                history.append(json.loads(line))
            except json.JSONDecodeError as e:
                print(f"⚠️ Error parseando línea {line_num}: {e}")
                print(f"   Contenido: {line[:100]}...")
                print(f"   Saltando línea inválida...")
                continue

if not history:
    print("⚠️ No hay datos para graficar.")
    exit()

print(f"📊 Generando 6 gráficos...")

# Formatear fechas (solo YYYY-MM-DD)
dates_raw = [h['date'] for h in history]
dates = [d.split()[0] for d in dates_raw]  # Extraer solo la fecha sin hora

def apply_weekend_colors(ax, dates):
    """Aplica colores de fondo a las etiquetas de sábados y domingos."""
    labels = ax.get_xticklabels()
    for i, (label, date_str) in enumerate(zip(labels, dates)):
        try:
            date_obj = datetime.strptime(date_str, '%Y-%m-%d')
            # weekday(): 5=sábado, 6=domingo
            if date_obj.weekday() in [5, 6]:
                label.set_bbox(dict(facecolor='#FFE5E5', edgecolor='#FF9999', 
                                   boxstyle='round,pad=0.3', alpha=0.7))
                label.set_weight('bold')
        except ValueError:
            pass  # Si no se puede parsear la fecha, continuar

# Paleta de colores distintivos y vibrantes
distinctive_colors = [
    '#FF1744',  # Rojo brillante
    '#2979FF',  # Azul brillante
    '#00E676',  # Verde brillante
    '#FF9100',  # Naranja brillante
    '#E040FB',  # Púrpura brillante
    '#00E5FF',  # Cian brillante
    '#FFD600',  # Amarillo brillante
    '#1DE9B6',  # Verde azulado
    '#FF6E40',  # Naranja rojizo
    '#7C4DFF',  # Violeta brillante
]

# --- Gráfico 1: Evolución de commits totales ---
# history.jsonl contiene acumulativos directamente
total_commits = [h['commits_total'] for h in history]

plt.figure(figsize=(12,6.5))
plt.plot(dates, total_commits, marker='o', label="Commits Totales", linewidth=3, markersize=9, color='#000000')
max_val = max(total_commits) if total_commits else 1
for x, y in zip(dates, total_commits):
    plt.text(x, y + (max_val * 0.03), str(y), ha='center', va='bottom', fontsize=11, fontweight='bold')

plt.xlabel("Data", fontsize=11)
plt.ylabel("Commits", fontsize=11)
plt.xticks(rotation=45, ha='right')
apply_weekend_colors(plt.gca(), dates)
plt.ylim(top=max_val * 1.12)  # Añadir 12% de padding arriba
plt.gca().yaxis.set_major_locator(MaxNLocator(integer=True))
plt.grid(True, alpha=0.3)
plt.subplots_adjust(bottom=0.15, top=0.96, left=0.08, right=0.96)
plt.savefig("stats/graphs/commits_total.png", bbox_inches='tight', dpi=100)
plt.close()

# --- Gráfico 2: Evolución de commits por usuario ---
# history.jsonl contiene acumulativos directamente
users = set()
for h in history:
    users.update(h['commits_by_user'].keys())
users = sorted(users)

plt.figure(figsize=(12,6.5))

if users:
    all_counts = []
    for idx, u in enumerate(users):
        counts = [h['commits_by_user'].get(u, 0) for h in history]
        all_counts.extend(counts)
        color = distinctive_colors[idx % len(distinctive_colors)]
        plt.plot(dates, counts, marker='o', label=u, linewidth=2.5, markersize=7, color=color)
    
    max_val = max(all_counts) if all_counts else 1
    for idx, u in enumerate(users):
        counts = [h['commits_by_user'].get(u, 0) for h in history]
        for x, y in zip(dates, counts):
            if y > 0:
                plt.text(x, y + (max_val * 0.03), str(y), ha='center', va='bottom', fontsize=11)
    
    plt.ylim(top=max_val * 1.12)  # Añadir 12% de padding arriba
    plt.legend(loc='best', fontsize=10)
else:
    plt.text(0.5, 0.5, 'Sin datos de usuarios', ha='center', va='center', transform=plt.gca().transAxes, fontsize=12)

plt.xlabel("Data", fontsize=11)
plt.ylabel("Commits", fontsize=11)
plt.xticks(rotation=45, ha='right')
apply_weekend_colors(plt.gca(), dates)
plt.gca().yaxis.set_major_locator(MaxNLocator(integer=True))
plt.grid(True, alpha=0.3)
plt.subplots_adjust(bottom=0.15, top=0.96, left=0.08, right=0.96)
plt.savefig("stats/graphs/commits_by_user.png", bbox_inches='tight', dpi=100)
plt.close()

# --- Gráfico 3: Commits por rama (evolución) ---
def get_default_branch(candidates):
    """Detectar adar lehenetsia: primero origin/HEAD, si falla probar 'main'/'master'."""
    try:
        ref = subprocess.check_output([
            'git', 'symbolic-ref', '--quiet', '--short', 'refs/remotes/origin/HEAD'
        ]).decode('utf-8').strip()
        if ref.startswith('origin/'):
            ref = ref[len('origin/'):]
        if ref in candidates:
            return ref
    except Exception:
        pass
    # Fallbacks
    if 'main' in candidates:
        return 'main'
    if 'master' in candidates:
        return 'master'
    return None
branches = set()
for h in history:
    branches.update(h['commits_by_branch'].keys())
branches = sorted(branches)
default_branch = get_default_branch(set(branches))
if default_branch and default_branch in branches:
    # Reordenar para que la rama principal vaya primero
    branches = [default_branch] + [b for b in branches if b != default_branch]

if branches:
    plt.figure(figsize=(12,6.5))
    
    # Colores distintivos y vibrantes para las ramas
    distinctive_branch_colors = [
        '#FF1744',  # Rojo brillante
        '#2979FF',  # Azul brillante
        '#00E676',  # Verde brillante
        '#FF9100',  # Naranja brillante
        '#E040FB',  # Púrpura brillante
        '#00E5FF',  # Cian brillante
        '#FFD600',  # Amarillo brillante
        '#1DE9B6',  # Verde azulado
        '#FF6E40',  # Naranja rojizo
        '#7C4DFF',  # Violeta brillante
        '#FF4081',  # Rosa brillante
        '#69F0AE',  # Verde claro brillante
    ]

    # Preparar colores, destacando la rama principal con color fijo y trazo más grueso
    colors_branch = []
    color_idx = 0
    for idx, b in enumerate(branches):
        if b == default_branch:
            colors_branch.append('#000000')  # negro para destacar
        else:
            colors_branch.append(distinctive_branch_colors[color_idx % len(distinctive_branch_colors)])
            color_idx += 1
    
    # Recopilar todos los valores para calcular el máximo global
    # history.jsonl contiene acumulativos directamente
    all_branch_counts = []
    for branch in branches:
        counts = [h['commits_by_branch'].get(branch, 0) for h in history]
        all_branch_counts.extend(counts)
    
    max_val = max(all_branch_counts) if all_branch_counts else 1
    
    for idx, branch in enumerate(branches):
        counts = [h['commits_by_branch'].get(branch, 0) for h in history]
        if branch == default_branch:
            plt.plot(dates, counts, marker='o', label=f"{branch} (principal)", linewidth=3.2, markersize=7, color=colors_branch[idx])
        else:
            plt.plot(dates, counts, marker='o', label=branch, linewidth=2, markersize=6, color=colors_branch[idx])
        # Añadir etiquetas en todos los puntos con valores
        for x, y in zip(dates, counts):
            if y > 0:
                plt.text(x, y + (max_val * 0.03), str(y), ha='center', va='bottom', fontsize=11)

    plt.xlabel("Data", fontsize=11)
    plt.ylabel("Commits", fontsize=11)
    plt.xticks(rotation=45, ha='right')
    apply_weekend_colors(plt.gca(), dates)
    plt.ylim(top=max_val * 1.12)  # Añadir 12% de padding arriba
    plt.legend(loc='best', fontsize=10)
    plt.gca().yaxis.set_major_locator(MaxNLocator(integer=True))
    plt.grid(True, alpha=0.3)
    plt.subplots_adjust(bottom=0.15, top=0.96, left=0.08, right=0.96)
    plt.savefig("stats/graphs/commits_by_branch.png", bbox_inches='tight', dpi=100)
    plt.close()
else:
    # Sin datos de ramas
    plt.figure(figsize=(12,6.5))
    plt.text(0.5, 0.5, 'Sin datos de ramas', 
            ha='center', va='center', transform=plt.gca().transAxes, fontsize=12)
    plt.savefig("stats/graphs/commits_by_branch.png", bbox_inches='tight', dpi=100)
    plt.close()

# --- Gráfico 4: Actividad semanal (últimos 7 días si hay suficientes datos) ---
# Calcular incrementos diarios desde acumulativos
if len(history) >= 2:
    # Calcular diferencia entre días consecutivos
    daily_increments = []
    for i in range(1, len(history)):
        increment = history[i]['commits_total'] - history[i-1]['commits_total']
        daily_increments.append(increment)
    
    # Tomar los últimos 7 días o todos si hay menos
    recent_dates = dates[-min(7, len(daily_increments)):]
    recent_increments = daily_increments[-min(7, len(daily_increments)):]
    
    plt.figure(figsize=(12,6.5))
    colors = ['#2ecc71' if x > 0 else '#95a5a6' for x in recent_increments]
    bars = plt.bar(range(len(recent_increments)), recent_increments, color=colors, alpha=0.8)
    
    max_increment = max(recent_increments) if recent_increments else 1
    # Añadir valores sobre las barras
    for i, (bar, val) in enumerate(zip(bars, recent_increments)):
        if val > 0:
            plt.text(bar.get_x() + bar.get_width()/2, val + (max_increment * 0.05), str(val), 
                    ha='center', va='bottom', fontsize=12, fontweight='bold')

    plt.xlabel("Epea", fontsize=11)
    plt.ylabel("Commits berriak", fontsize=11)
    plt.xticks(range(len(recent_dates)), recent_dates, rotation=45, ha='right')
    apply_weekend_colors(plt.gca(), recent_dates)
    plt.ylim(top=max_increment * 1.15)  # Añadir 15% de padding arriba para las barras
    plt.gca().yaxis.set_major_locator(MaxNLocator(integer=True))
    plt.grid(True, axis='y', alpha=0.3)
    plt.axhline(y=0, color='black', linewidth=0.8)
    plt.subplots_adjust(bottom=0.15, top=0.96, left=0.08, right=0.96)
    plt.savefig("stats/graphs/weekly_activity.png", bbox_inches='tight', dpi=100)
    plt.close()
else:
    # Si solo hay un dato, crear gráfico vacío con mensaje
    plt.figure(figsize=(12,6.5))
    plt.text(0.5, 0.5, 'Esperando más datos para mostrar actividad', 
            ha='center', va='center', transform=plt.gca().transAxes, fontsize=12)
    plt.savefig("stats/graphs/weekly_activity.png", bbox_inches='tight', dpi=100)
    plt.close()

# --- Gráfico 5: Distribución de commits por usuario (pie chart) ---
# Usar el último registro (ya contiene acumulativos)
if history:
    latest = history[-1]
    user_data = latest['commits_by_user']
    
    if user_data and sum(user_data.values()) > 0:
        plt.figure(figsize=(12,10))
        
        # Ordenar usuarios por cantidad de commits
        sorted_users = sorted(user_data.items(), key=lambda x: x[1], reverse=True)
        labels = [u[0] for u in sorted_users]
        sizes = [u[1] for u in sorted_users]
        total = sum(sizes)
        percentages = [100 * s / total for s in sizes]
        
        # Colores distintos para cada usuario
        colors = plt.cm.Set3(np.linspace(0, 1, len(labels)))
        
        # Crear gráfico de pastel SIN etiquetas automáticas
        wedges, texts = plt.pie(sizes, colors=colors, startangle=90, 
                                labels=None, autopct=None)
        
        # Calcular radios adaptativos para evitar solapamientos
        # Sectores pequeños (<5%) necesitan radios alternos TANTO para porcentajes como nombres
        pct_radii = []
        label_radii = []
        for i, pct in enumerate(percentages):
            if pct < 5:
                # Alternar entre diferentes distancias para sectores pequeños
                # Porcentajes: radios entre 0.5 y 0.75
                pct_radii.append(0.5 + (i % 3) * 0.12)
                # Nombres: radios entre 1.25 y 1.55
                label_radii.append(1.25 + (i % 3) * 0.15)
            else:
                pct_radii.append(0.7)
                label_radii.append(1.2)
        
        # Añadir NOMBRES y PORCENTAJES fuera del sector
        for i, (wedge, label, size, pct) in enumerate(zip(wedges, labels, sizes, percentages)):
            # Calcular el ángulo medio del sector
            ang = (wedge.theta2 - wedge.theta1) / 2. + wedge.theta1
            rad_ang = np.deg2rad(ang)
            
            # NOMBRE Y PORCENTAJE fuera del sector (radio adaptativo)
            label_radius = label_radii[i]
            label_x = label_radius * np.cos(rad_ang)
            label_y = label_radius * np.sin(rad_ang)
            
            # Alineación según posición
            ha = 'left' if label_x > 0 else 'right'
            
            # Mostrar nombre con porcentaje debajo en todos los casos
            label_text = f"{label}\n({pct:.1f}%)"
            
            plt.text(label_x, label_y, label_text,
                    ha=ha, va='center',
                    fontsize=10,
                    fontweight='bold',
                    color='black')
            
            # Línea de conexión desde el borde hasta el nombre
            conn_x = 1.05 * np.cos(rad_ang)
            conn_y = 1.05 * np.sin(rad_ang)
            plt.plot([conn_x, label_x], [conn_y, label_y], 
                    color='gray', linewidth=1, linestyle='-', alpha=0.6)

        plt.axis('equal')
        plt.tight_layout()
        plt.savefig("stats/graphs/commits_distribution.png", bbox_inches='tight', dpi=100)
        plt.close()
    else:
        # Sin datos
        plt.figure(figsize=(9,7))
        plt.text(0.5, 0.5, 'Sin datos de commits por usuario', 
                ha='center', va='center', transform=plt.gca().transAxes, fontsize=12)
        plt.savefig("stats/graphs/commits_distribution.png", bbox_inches='tight', dpi=100)
        plt.close()

# --- Gráfico 6: Evolución de issues ---
# Leer directamente los totales (nuevo formato) o calcularlos (formato antiguo para compatibilidad)
open_total = []
closed_total = []

for h in history:
    # Intentar leer el nuevo formato primero
    if 'open' in h['issues']:
        open_total.append(h['issues'].get('open', 0))
        closed_total.append(h['issues'].get('closed', 0))
    else:
        # Fallback al formato antiguo
        open_total.append(h['issues'].get('open_assigned', 0) + h['issues'].get('open_unassigned', 0))
        closed_total.append(h['issues'].get('closed_assigned', 0) + h['issues'].get('closed_unassigned', 0))

plt.figure(figsize=(12,6.5))
plt.plot(dates, open_total, marker='o', label='Irekiak', linewidth=2, markersize=6)
plt.plot(dates, closed_total, marker='o', label='Itxiak', linewidth=2, markersize=6)

# Calcular max para separación
all_issues = open_total + closed_total
max_issue = max(all_issues) if all_issues and max(all_issues) > 0 else 1

for x, ylist in zip(dates, zip(open_total, closed_total)):
    for y in ylist:
        if y > 0:
            plt.text(x, y + (max_issue * 0.04), str(y), ha='center', va='bottom', fontsize=11)

plt.xlabel("Data", fontsize=11)
plt.ylabel("Issues kopurua", fontsize=11)
plt.xticks(rotation=45, ha='right')
apply_weekend_colors(plt.gca(), dates)
plt.ylim(top=max_issue * 1.12)  # Añadir 12% de padding arriba
plt.legend(loc='best', fontsize=10)
plt.gca().yaxis.set_major_locator(MaxNLocator(integer=True))
plt.grid(True, alpha=0.3)
plt.subplots_adjust(bottom=0.15, top=0.96, left=0.08, right=0.96)
plt.savefig("stats/graphs/issues.png", bbox_inches='tight', dpi=100)
plt.close()

print("✅ Todos los gráficos generados exitosamente en stats/graphs/")
