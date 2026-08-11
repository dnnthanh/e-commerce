import { Component } from '@angular/core';
import { AdminShellComponent } from './layout/admin-shell.component';

/** Root only hosts the focused administration shell. */
@Component({
  standalone: true,
  selector: 'app-root',
  imports: [AdminShellComponent],
  template: `<admin-shell />`,
})
export class AppComponent {}
