import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-storage-modal',
  templateUrl: './storage-modal.page.html'
})
export class StorageModalPage implements OnInit {

  // show the chart for today
  public fromDate = new Date();
  public toDate = new Date();


  constructor(
    private modalCtrl: ModalController,
  ) { }

  ngOnInit() {
  }

  cancel() {
    this.modalCtrl.dismiss();
  }

}
