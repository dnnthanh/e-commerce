import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../../../shared/api.service';
/** Operations landing page backed by incident/audit APIs with links to external observability UIs. */
@Component({ standalone: true, selector: 'admin-dashboard', imports: [CommonModule, RouterLink], template: `
<section><div class="page-title"><div><small>MARKETPLACE CONTROL PLANE</small><h1>Operations Dashboard</h1></div></div>
<div class="cards"><article><span>Open incidents</span><strong>{{incidents().length}}</strong><small>Recovery center</small></article><article><span>Critical / high</span><strong>{{high()}}</strong><small>Needs attention</small></article><article><span>Architecture</span><strong>53</strong><small>be-* deployables</small></article><article><span>Observability</span><strong>LGTM</strong><small>metrics · logs · traces</small></article></div>
<div class="panel"><div class="panel-title"><h2>System tools</h2></div><div class="tool-grid"><a href="http://localhost:3001" target="_blank"><strong>Grafana</strong><span>Metrics, Loki logs, Tempo traces</span></a><a href="http://localhost:8185" target="_blank"><strong>Kafka UI</strong><span>Topics, partitions, messages, consumer groups</span></a><a href="http://localhost:5601" target="_blank"><strong>OpenSearch Dashboards</strong><span>Search read model inspection</span></a><a href="http://localhost:8180" target="_blank"><strong>Keycloak</strong><span>Identity and role tree</span></a></div></div>
<div class="panel"><div class="panel-title"><h2>Latest incidents</h2><a routerLink="/operations">Open recovery center →</a></div><table><thead><tr><th>Type</th><th>Source</th><th>Severity</th><th>Aggregate</th></tr></thead><tbody><tr *ngFor="let i of incidents().slice(0,8)"><td>{{i.type}}</td><td>{{i.sourceService}}</td><td><span class="badge">{{i.severity}}</span></td><td>{{i.aggregateId}}</td></tr></tbody></table></div></section>` })
export class DashboardComponent implements OnInit {
    readonly incidents = signal<any[]>([]);
    constructor(private readonly api: ApiService) { }
    async ngOnInit() { try {
        this.incidents.set(await this.api.get<any[]>('/private/operations/incidents', true));
    }
    catch {
        this.incidents.set([]);
    } }
    high() { return this.incidents().filter(i => ['CRITICAL', 'HIGH'].includes(i.severity)).length; }
}
