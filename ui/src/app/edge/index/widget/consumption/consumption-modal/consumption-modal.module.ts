import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';

import { IonicModule } from '@ionic/angular';

import { ConsumptionModalPage } from './consumption-modal.page';

const routes: Routes = [
  {
    path: '',
    component: ConsumptionModalPage
  }
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RouterModule.forChild(routes)
  ],
  declarations: [ConsumptionModalPage]
})
export class ConsumptionModalPageModule {}
