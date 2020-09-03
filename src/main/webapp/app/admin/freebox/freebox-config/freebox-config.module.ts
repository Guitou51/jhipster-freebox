import { NgModule } from '@angular/core';
import { FreeboxConfigComponent } from './freebox-config.component';
import { RouterModule } from '@angular/router';
import { freeboxRoute } from './freebox.route';
import { JhipsterFreeboxSharedModule } from '../../../shared/shared.module';

@NgModule({
  imports: [JhipsterFreeboxSharedModule, RouterModule.forChild([freeboxRoute])],
  declarations: [FreeboxConfigComponent],
})
export class FreeboxConfigModule {}
