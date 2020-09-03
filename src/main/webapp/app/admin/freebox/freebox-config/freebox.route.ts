import { Route } from '@angular/router';

import { FreeboxConfigComponent } from './freebox-config.component';

export const freeboxRoute: Route = {
  path: '',
  component: FreeboxConfigComponent,
  data: {
    pageTitle: 'freebox.title',
    // defaultSort: 'auditEventDate,desc',
  },
};
