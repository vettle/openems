import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ChannelAddress, Edge, Service, Websocket } from '../../../../shared/shared';

@Component({
    selector: 'storage',
    templateUrl: './storage.component.html'
})
export class StorageComponent {

    private static readonly SELECTOR = "storage";


    public edge: Edge = null;

    constructor(
        private service: Service,
        private websocket: Websocket,
        private route: ActivatedRoute
    ) { }

    ngOnInit() {
        this.service.setCurrentEdge(this.route).then(edge => {
            this.edge = edge;
            edge.subscribeChannels(this.websocket, StorageComponent.SELECTOR, [
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
            this.edge.unsubscribeChannels(this.websocket, StorageComponent.SELECTOR);
        }
    }
}
