import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ChannelAddress, Edge, Service, Websocket } from '../../../../shared/shared';
import { ModalController } from '@ionic/angular';
import { ProductionModalPage } from './production-modal/production-modal.page';

@Component({
    selector: 'production',
    templateUrl: './production.component.html'
})
export class ProductionComponent {

    private static readonly SELECTOR = "production";


    public edge: Edge = null;

    constructor(
        private service: Service,
        private websocket: Websocket,
        private route: ActivatedRoute,
        private modalController: ModalController
    ) { }

    ngOnInit() {
        this.service.setCurrentEdge(this.route).then(edge => {
            this.edge = edge;
            edge.subscribeChannels(this.websocket, ProductionComponent.SELECTOR, [
                // Ess
                new ChannelAddress('_sum', 'EssSoc'), new ChannelAddress('_sum', 'EssActivePower'),
                // Grid
                new ChannelAddress('_sum', 'GridActivePower'),
                // Production
                new ChannelAddress('_sum', 'ProductionActivePower'), new ChannelAddress('_sum', 'ProductionDcActualPower'), new ChannelAddress('_sum', 'ProductionAcActivePower'), new ChannelAddress('_sum', 'ProductionMaxActivePower'),
                // Consumption
                new ChannelAddress('_sum', 'ConsumptionActivePower'), new ChannelAddress('_sum', 'ConsumptionMaxActivePower')
            ]);
        });
    }

    ngOnDestroy() {
        if (this.edge != null) {
            this.edge.unsubscribeChannels(this.websocket, ProductionComponent.SELECTOR);
        }
    }

    async presentModal() {
        const modal = await this.modalController.create({
            component: ProductionModalPage,
        });
        return await modal.present();
    }
}
