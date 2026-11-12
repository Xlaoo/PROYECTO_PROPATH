(() => {
    'use strict';
    const palette = ['#1e5ce6', '#008ca8', '#00a376', '#9460bc', '#d88b12'];
    const format = new Intl.NumberFormat('es-PE');

    function node(tag, className, text) {
        const element = document.createElement(tag);
        if (className) element.className = className;
        if (text !== undefined) element.textContent = text;
        return element;
    }

    function count(value) {
        const number = Number(value);
        return Number.isFinite(number) && number > 0 ? number : 0;
    }

    function empty(container) {
        container.append(node('p', 'dashboard-chart-empty', 'Sin datos para este gráfico.'));
    }

    function donut(id, entries, total) {
        const container = document.getElementById(id);
        container.replaceChildren();
        if (!total) return empty(container);

        const parts = entries.map(([label, value], index) => ({
            label, value: count(value), color: palette[index]
        }));
        const classified = parts.reduce((sum, entry) => sum + entry.value, 0);
        if (classified < total) {
            parts.push({label: 'Sin clasificar', value: total - classified, color: '#8797ab'});
        }

        const ring = node('div', 'dashboard-chart-ring');
        ring.setAttribute('aria-hidden', 'true');
        const stops = [];
        let start = 0;
        const denominator = Math.max(total, classified);
        for (const part of parts) {
            if (!part.value) continue;
            const end = start + part.value * 100 / denominator;
            stops.push(part.color + ' ' + start + '% ' + end + '%');
            start = end;
        }
        ring.style.backgroundImage = 'conic-gradient(' + stops.join(', ') + ')';
        ring.append(node('span', '', format.format(total)));
        const legend = node('ul', 'dashboard-chart-legend');
        for (const part of parts) {
            const row = node('li', '');
            const swatch = node('span', 'dashboard-chart-swatch');
            swatch.style.backgroundColor = part.color;
            swatch.setAttribute('aria-hidden', 'true');
            row.append(swatch, node('span', '', part.label),
                node('strong', '', format.format(part.value) + ' · ' + Math.round(part.value * 100 / denominator) + '%'));
            legend.append(row);
        }
        container.append(ring, legend);
    }

    function bars(id, entries) {
        const container = document.getElementById(id);
        container.replaceChildren();
        const maximum = Math.max(0, ...entries.map(([, value]) => count(value)));
        if (!maximum) return empty(container);
        const list = node('ul', 'dashboard-chart-bars');
        for (const [label, rawValue] of entries) {
            const value = count(rawValue);
            const row = node('li', '');
            const caption = node('div', 'dashboard-chart-caption');
            caption.append(node('span', '', label), node('strong', '', format.format(value)));
            const track = node('div', 'dashboard-chart-track');
            const bar = node('span', '');
            bar.style.width = (value * 100 / maximum) + '%';
            track.setAttribute('aria-hidden', 'true');
            track.append(bar);
            row.append(caption, track);
            list.append(row);
        }
        container.append(list);
    }

    function monthly(id, series) {
        const container = document.getElementById(id);
        container.replaceChildren();
        const entries = (series || []).slice(-12);
        const maximum = Math.max(0, ...entries.map(item => count(item.cantidad)));
        if (!maximum) return empty(container);
        const scale = Math.max(1, Math.ceil(maximum / 4));
        const ceiling = scale * 4;
        const frame = node('div', 'dashboard-chart-frame');
        const axis = node('div', 'dashboard-chart-axis');
        axis.setAttribute('aria-hidden', 'true');
        for (let tick = 4; tick >= 0; tick--) {
            axis.append(node('span', '', format.format(tick * scale)));
        }
        const list = node('ul', 'dashboard-chart-columns');
        for (const item of entries) {
            const value = count(item.cantidad);
            const column = node('li', '');
            const plot = node('div', 'dashboard-chart-column-plot');
            const bar = node('span', '');
            bar.style.height = (value * 100 / ceiling) + '%';
            const label = node('strong', '', format.format(value));
            label.style.bottom = (value * 100 / ceiling) + '%';
            plot.append(bar, label);
            column.append(plot, node('span', 'dashboard-chart-month', item.mes));
            list.append(column);
        }
        frame.append(axis, list);
        container.append(frame);
    }

    window.graficosPropath = {
        estudiantes(data) {
            const total = count(data.total);
            donut('graficoCuentas', [['Activas', data.activos], ['Inactivas', data.inactivos],
                ['Bloqueadas', data.bloqueados]], total);
            donut('graficoCobertura', [['Con CV', data.conCv], ['Sin CV', data.sinCv]], total);
            monthly('graficoAltas', data.tendenciaMeses);
        },
        empresas(data) {
            const total = count(data.total);
            donut('graficoCuentas', [['Activas', data.cuentasActivas], ['Inactivas', data.cuentasInactivas],
                ['Bloqueadas', data.cuentasBloqueadas]], total);
            const activas = count(data.conOfertasActivas);
            donut('graficoCobertura', [['Con ofertas activas', activas],
                ['Solo ofertas no activas', Math.max(0, count(data.conOfertas) - activas)],
                ['Sin ofertas', data.sinOfertas]], total);
            monthly('graficoAltas', data.tendenciaMeses);
            bars('graficoRanking', (data.rankingEmpresasOfertas || []).slice(0, 8)
                .map(item => [item.empresa, item.ofertas]));
        },
        administradores(data) {
            const total = count(data.total);
            donut('graficoEstado', [
                ['Activos', data.activos],
                ['Inactivos', data.inactivos],
                ['Bloqueados', data.bloqueados]
            ], total);
            bars('graficoCargos', (data.distribucionCargos || []).map(item => [item.cargo, item.cantidad]));
        }
    };
})();
