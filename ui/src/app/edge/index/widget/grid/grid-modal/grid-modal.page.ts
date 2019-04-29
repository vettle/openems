import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-grid-modal',
  templateUrl: './grid-modal.page.html'
})
export class GridModalPage implements OnInit {

  constructor(
    private modalCtrl: ModalController,
  ) { }

  ngOnInit() {
  }

  cancel() {
    this.modalCtrl.dismiss();
  }

}
