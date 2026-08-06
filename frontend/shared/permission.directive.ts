import { Directive, Input, TemplateRef, ViewContainerRef, effect } from '@angular/core';
import { AuthService } from './auth.service';
/** Conditionally renders UI elements using the authorization snapshot; backend authorization remains authoritative. */
@Directive({ selector: '[appHasPermission]', standalone: true })
export class HasPermissionDirective {
    /** Permission required to render the host template. */
    @Input()
    set appHasPermission(value: string) { this.permission = value; this.render(); }
    /** Required permission. */
    private permission = '';
    /** Whether the embedded view is currently attached. */
    private rendered = false;
    /** Creates the directive. */
    constructor(private readonly auth: AuthService, private readonly template: TemplateRef<unknown>, private readonly container: ViewContainerRef) {
        effect(() => { this.auth.authorization(); this.render(); });
    }
    private render(): void {
        const allowed = !!this.permission && this.auth.has(this.permission);
        if (allowed && !this.rendered) {
            this.container.createEmbeddedView(this.template);
            this.rendered = true;
        }
        if (!allowed && this.rendered) {
            this.container.clear();
            this.rendered = false;
        }
    }
}
